package com.github.sunnysuperman.pimsdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLSocketFactory;

import com.github.sunnysuperman.pimsdk.keepalive.KeepAlivePolicy;
import com.github.sunnysuperman.pimsdk.packet.ConnectPacket;
import com.github.sunnysuperman.pimsdk.packet.DefaultPacket;
import com.github.sunnysuperman.pimsdk.packet.Message;
import com.github.sunnysuperman.pimsdk.packet.MessageAck;
import com.github.sunnysuperman.pimsdk.packet.MessageReceipt;
import com.github.sunnysuperman.pimsdk.packet.PongPacket;
import com.github.sunnysuperman.pimsdk.util.GZipUtil;
import com.github.sunnysuperman.pimsdk.util.PacketUtil;
import com.github.sunnysuperman.pimsdk.util.PimUtil;

public class PimClient implements PimClientError {
	private final byte[] LOCK = new byte[0];
	private final byte[] WRITE_LOCK = new byte[0];
	private final PimLogger logger;
	private volatile Socket socket;
	private OutputStream out;
	private InputStream in;
	private volatile PimClientOptions options;
	private ReadThread readThread;
	private Timer timer;
	private long[] lastSequenceID = null;
	private int disconnectReason;
	private LinkedList<Message> pendingMessages = new LinkedList<Message>();
	private int compressThreshold;
	private KeepAlivePolicy keepAlivePolicy;

	private class CheckSendTimeoutTask extends TimerTask {

		@Override
		public void run() {
			int timeoutSeconds = options.getSendTimeoutSeconds();
			if (timeoutSeconds <= 0 || timeoutSeconds > 120) {
				timeoutSeconds = 30;
			}
			long timeout = System.currentTimeMillis() - timeoutSeconds * 1000L;
			List<Message> timeoutMsgs = new LinkedList<Message>();
			synchronized (LOCK) {
				Iterator<Message> iter = pendingMessages.iterator();
				while (iter.hasNext()) {
					Message msg = iter.next();
					if (msg.getTime().getTime() < timeout) {
						timeoutMsgs.add(msg);
						iter.remove();
					}
				}
			}
			if (!timeoutMsgs.isEmpty()) {
				try {
					options.onSendMessageTimeout(timeoutMsgs);
				} catch (Exception ex) {
					logger.error(ex);
				}
			}
		}
	}

	private Packet readPacket() {
		try {
			byte[] headerBuf = new byte[4];
			int len = read(in, headerBuf, 0, 1);
			if (len != 1) {
				return null;
			}
			byte metadata = headerBuf[0];
			boolean hasData = ((metadata >> 7) & 0x1) > 0;
			if (!hasData) {
				return new DefaultPacket(PacketUtil.getPacketType(metadata), null);
			}
			len = read(in, headerBuf, 0, 3);
			if (len != 3) {
				return null;
			}
			int bodySize = (headerBuf[2] & 0xFF) + (headerBuf[1] & 0xFF) * 256 + (headerBuf[0] & 0xFF) * 65536;
			byte packetType = PacketUtil.getPacketType(metadata);
			if (bodySize == 0) {
				return new DefaultPacket(packetType, null);
			}
			byte[] body = new byte[bodySize];
			len = read(in, body, 0, bodySize);
			logger.info("packetType: " + packetType + ", bodySize: " + bodySize + ", actualBodySize: " + len);
			if (len != bodySize) {
				return null;
			}
			boolean compress = ((metadata >> 6) & 0x1) > 0;
			if (compress) {
				body = GZipUtil.decompress(body);
				if (logger.isInfoEnabled()) {
					logger.info("decompress: " + bodySize + "->" + body.length);
				}
			}
			return new DefaultPacket(packetType, body);
		} catch (Throwable t) {
			logger.error("Failed to read packet", t);
			return null;
		}
	}

	private int read(InputStream in, byte b[], int off, int len) throws IOException {
		int c = in.read();
		if (c == -1) {
			logger.error("Read end");
			return -1;
		}
		b[off] = (byte) c;

		int i = 1;
		try {
			for (; i < len; i++) {
				c = in.read();
				if (c == -1) {
					logger.error("Read end accidently");
					break;
				}
				b[off + i] = (byte) c;
			}
		} catch (IOException ee) {
		}
		return i;
	}

	private class ReadThread extends Thread {
		private boolean invalidated;

		public void invalidate() {
			invalidated = true;
		}

		@Override
		public void run() {
			while (true) {
				Packet packet = readPacket();
				if (packet == null) {
					break;
				}
				try {
					onPacket(packet);
				} catch (Exception e) {
					logger.error("Process packet error", e);
					break;
				}
			}
			DisconnectInfo disconnectInfo = null;
			synchronized (LOCK) {
				if (!invalidated) {
					disconnectInfo = distroy();
				}
			}
			if (disconnectInfo != null) {
				onDisconnected(disconnectInfo);
			}

		}
	}

	private void onDisconnected(DisconnectInfo disconnectInfo) {
		try {
			options.onDisconnected(disconnectInfo);
		} catch (Exception ex) {
			logger.error(ex);
		}
		if (keepAlivePolicy != null) {
			keepAlivePolicy.onDisconnected(disconnectInfo);
		}
	}

	private DisconnectInfo distroy() {
		DisconnectInfo info = null;
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				logger.error(e);
			}
			socket = null;
			info = new DisconnectInfo();
			info.setPendingMessages(pendingMessages);
			pendingMessages = new LinkedList<Message>();
			info.setReason(disconnectReason);
			disconnectReason = 0;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (readThread != null) {
			readThread.invalidate();
			readThread = null;
		}
		return info;
	}

	private int doConnect() {
		try {
			if (options.isSsl()) {
				SSLSocketFactory sslSocketFactory = options.getSslSocketFactory();
				if (sslSocketFactory == null) {
					return ERR_CONNECT_CLIENT;
				}
				socket = sslSocketFactory.createSocket();
			} else {
				socket = new Socket();
			}
			int timeout = options.getConnectTimeoutSeconds();
			if (timeout <= 0) {
				timeout = 30;
			}
			InetSocketAddress address = new InetSocketAddress(options.getServerHost(), options.getServerPort());
			socket.connect(address, timeout * 1000);
			socket.setSoTimeout(timeout * 1000);
			out = socket.getOutputStream();
			in = socket.getInputStream();
			if (!writeConnectPacket()) {
				return ERR_CONNECT_NETWORK;
			}
			while (true) {
				Packet packet = readPacket();
				if (packet == null) {
					return ERR_CONNECT_NETWORK;
				}
				if (packet.getType() != PacketType.TYPE_CONNECT_ACK) {
					continue;
				}
				LoginResponse response = options.parseLoginResponse(packet.getBody());
				int errorCode = response.getErrorCode();
				if (errorCode != 0) {
					return errorCode;
				}
				compressThreshold = response.getCompressThreshold();
				socket.setSoTimeout(0);
				return errorCode;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return ERR_CONNECT_NETWORK;
		}
	}

	private boolean writeConnectPacket() {
		byte[] body = options.makeLoginRequest();
		ConnectPacket packet = new ConnectPacket(body);
		return sendPacket(packet);
	}

	public PimClient(PimClientOptions options) {
		super();
		if (options.getLogger() == null) {
			throw new IllegalArgumentException("Bad options");
		}
		this.options = options;
		this.logger = options.getLogger();
		keepAlivePolicy = options.getKeepAlivePolicyFactory() != null ? options.getKeepAlivePolicyFactory()
				.create(this) : null;
	}

	private void onPacket(Packet packet) {
		byte type = packet.getType();
		if (logger.isInfoEnabled()) {
			logger.info("receive packet: " + type);
		}
		if (keepAlivePolicy != null) {
			keepAlivePolicy.onReadPacket(packet);
		}
		byte[] body = packet.getBody();
		switch (type) {
		case PacketType.TYPE_MSG: {
			Message msg = Message.deserialize(body);
			boolean processed = false;
			try {
				processed = options.onPacket(msg);
			} catch (Exception ex) {
				logger.error(ex);
			}
			if (processed && msg.getMsgID() != null) {
				sendMessageReadReceipt(msg.getMsgID());
			}
			break;
		}
		case PacketType.TYPE_MSG_ACK: {
			MessageAck ack = MessageAck.deserialize(body);
			if (options.isSendTimeoutCheck()) {
				synchronized (LOCK) {
					Iterator<Message> iter = pendingMessages.iterator();
					while (iter.hasNext()) {
						Message msg = iter.next();
						String sequenceID = msg.getSequenceID();
						if (sequenceID.equals(ack.getSequenceID())) {
							iter.remove();
							break;
						}
					}
				}
			}
			options.onPacket(ack);
			break;
		}
		case PacketType.TYPE_PING: {
			PongPacket pong = new PongPacket();
			sendPacket(pong);
			break;
		}
		case PacketType.TYPE_PONG: {
			PongPacket pong = new PongPacket();
			options.onPacket(pong);
			break;
		}
		case PacketType.TYPE_DISCONNECT: {
			synchronized (LOCK) {
				disconnectReason = PimUtil.bytes2int(body);
			}
			break;
		}
		default:
			break;
		}
	}

	public int connect() {
		synchronized (LOCK) {
			if (readThread != null) {
				return ERR_CONNECT_ALREADY_CONNECTED;
			}
			int error = doConnect();
			if (error != 0) {
				distroy();
				return error;
			}
			try {
				if (options.isSendTimeoutCheck()) {
					timer = new Timer("pim-send-timeout-timer");
					timer.schedule(new CheckSendTimeoutTask(), 0, 5 * 1000);
				}
				readThread = new ReadThread();
				readThread.start();
			} catch (Exception e) {
				logger.error(e);
				distroy();
				return ERR_CONNECT_CLIENT;
			}
		}
		if (keepAlivePolicy != null) {
			keepAlivePolicy.onConnected();
		}
		return 0;
	}

	public void disconnect(boolean force) {
		if (force && socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		DisconnectInfo disconnectInfo = null;
		synchronized (LOCK) {
			disconnectInfo = distroy();
		}
		if (disconnectInfo != null) {
			onDisconnected(disconnectInfo);
		}
	}

	public void disconnect() {
		disconnect(false);
	}

	public boolean isConnected() {
		synchronized (LOCK) {
			return socket != null;
		}
	}

	public boolean sendMessage(Message msg) throws Exception {
		return sendMessage(msg, options.isSendReceipt());
	}

	public boolean sendMessage(Message msg, boolean receipt) throws Exception {
		if (msg.getTo() == null) {
			throw new IllegalArgumentException("msg.to could not be null");
		}
		if (msg.getContent() == null) {
			throw new IllegalArgumentException("msg.content could not be null");
		}
		boolean ok = false;
		Message pendingMsg = null;
		try {
			if (msg.getSequenceID() == null) {
				if (receipt) {
					String autoSequenceID = null;
					synchronized (LOCK) {
						long t = System.currentTimeMillis() / 1000;
						long[] sequenceID = new long[] { t, 1 };
						if (lastSequenceID != null && lastSequenceID[0] == t) {
							sequenceID[1] = lastSequenceID[1] + 1;
						}
						autoSequenceID = sequenceID[0] + "-" + sequenceID[1];
						lastSequenceID = sequenceID;
					}
					msg.setSequenceID(autoSequenceID);
				}
			}
			msg.setMsgID(null);
			if (msg.getTime() == null) {
				msg.setTime(new Date());
			}
			msg.marshall();

			// clone and add to pending messages
			boolean timeoutCheck = options.isSendTimeoutCheck();
			if (timeoutCheck && msg.getSequenceID() != null) {
				pendingMsg = msg.clone();
				synchronized (LOCK) {
					pendingMessages.add(pendingMsg);
				}
			}
			ok = sendPacket(msg);
			return ok;
		} catch (Throwable t) {
			logger.error(t);
			return false;
		} finally {
			if (!ok) {
				if (pendingMsg != null) {
					synchronized (LOCK) {
						pendingMessages.remove(pendingMsg);
					}
				}
			}
		}
	}

	public boolean sendMessageReadReceipt(String msgID) {
		MessageReceipt packet = new MessageReceipt(msgID);
		return sendPacket(packet);
	}

	public boolean sendPacket(Packet packet) {
		if (logger.isInfoEnabled()) {
			logger.info("send packet: " + packet.getType());
		}
		if (keepAlivePolicy != null) {
			keepAlivePolicy.onWritePacket(packet);
		}
		try {
			byte[] body = packet.getBody();
			int bodyLen = body == null ? 0 : body.length;
			boolean compress = options.isCompressEnabled() && compressThreshold > 0 && bodyLen >= compressThreshold;
			if (compress) {
				byte[] compressBody = GZipUtil.compress(body);
				if (compressBody.length >= body.length) {
					logger.error("compress body size >= original body size, use original body instead");
					compress = false;
				} else {
					body = compressBody;
					bodyLen = body.length;
				}
			}
			byte[] header = PacketUtil.makeHeader(packet.getType(), bodyLen, compress);
			synchronized (WRITE_LOCK) {
				out.write(header);
				if (body != null) {
					out.write(body);
				}
			}
			return true;
		} catch (Throwable t) {
			logger.error(t);
			return false;
		}
	}

	public PimClientOptions getOptions() {
		return options;
	}

}
