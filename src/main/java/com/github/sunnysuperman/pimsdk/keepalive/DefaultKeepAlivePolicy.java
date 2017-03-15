package com.github.sunnysuperman.pimsdk.keepalive;

import java.util.Timer;
import java.util.TimerTask;

import com.github.sunnysuperman.pimsdk.DisconnectInfo;
import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PimClient;
import com.github.sunnysuperman.pimsdk.PimLogger;
import com.github.sunnysuperman.pimsdk.packet.PingPacket;

public class DefaultKeepAlivePolicy implements KeepAlivePolicy {
	public static class DefaultKeepAlivePolicyOptions {
		private int readTimeout = 90000;
		private int waitPongTimeout = 8000;
		private byte maxAllowIdleTimes = (byte) 2;

		public int getReadTimeout() {
			return readTimeout;
		}

		public DefaultKeepAlivePolicyOptions setReadTimeout(int readTimeout) {
			if (readTimeout <= 0) {
				throw new IllegalArgumentException("readTimeout");
			}
			this.readTimeout = readTimeout;
			return this;
		}

		public int getWaitPongTimeout() {
			return waitPongTimeout;
		}

		public DefaultKeepAlivePolicyOptions setWaitPongTimeout(int waitPongTimeout) {
			if (waitPongTimeout <= 0) {
				throw new IllegalArgumentException("waitPongTimeout");
			}
			this.waitPongTimeout = waitPongTimeout;
			return this;
		}

		public byte getMaxAllowIdleTimes() {
			return maxAllowIdleTimes;
		}

		public DefaultKeepAlivePolicyOptions setMaxAllowIdleTimes(byte maxAllowIdleTimes) {
			if (maxAllowIdleTimes <= 0) {
				throw new IllegalArgumentException("maxAllowIdleTimes");
			}
			this.maxAllowIdleTimes = maxAllowIdleTimes;
			return this;
		}

	}

	private final byte[] LOCK = new byte[0];
	private final PimClient client;
	private final DefaultKeepAlivePolicyOptions options;
	private final PimLogger logger;
	private volatile Timer timer;
	private volatile long lastReadTime;
	private volatile byte idleTimes;
	private volatile TimerTask currentTask;
	private volatile boolean stopped;

	private void info(String msg) {
		if (logger.isInfoEnabled()) {
			logger.info("[KeepAlive] " + msg);
		}
	}

	private class ReaderIdleTimeoutTask extends TimerTask {

		@Override
		public void run() {
			synchronized (LOCK) {
				currentTask = null;
			}
			try {
				if (!client.isConnected()) {
					info("disconnected when ReaderIdleTimeoutTask execute");
					return;
				}
				long nextDelay = options.readTimeout - (System.currentTimeMillis() - lastReadTime);
				if (nextDelay <= 0) {
					// Reader is idle - send ping and set a new timeout.
					idleTimes++;
					if (logger.isInfoEnabled()) {
						logger.info("read idle: " + idleTimes);
					}
					if (idleTimes >= options.maxAllowIdleTimes) {
						info("read timeout, so close connection");
						client.disconnect();
						return;
					}
					info("send ping to keep alive");
					client.sendPacket(new PingPacket());
					scheduleTimeoutTask(options.waitPongTimeout, false);
				} else {
					// Read occurred before the timeout
					idleTimes = 0;
					scheduleTimeoutTask(nextDelay, false);
				}
			} catch (Throwable t) {
				logger.error("ReaderIdleTimeoutTask execute failed", t);
				client.disconnect();
			}
		}
	}

	private void scheduleTimeoutTask(long timeout, boolean force) {
		synchronized (LOCK) {
			if (stopped) {
				if (!force) {
					return;
				}
				stopped = false;
			}
			if (timer == null) {
				timer = new Timer("pim-keep-alive");
			}
			if (currentTask != null) {
				currentTask.cancel();
			}
			currentTask = new ReaderIdleTimeoutTask();
			while (true) {
				// case schedule failed
				try {
					timer.schedule(currentTask, timeout);
					if (logger.isInfoEnabled()) {
						info("scheduleTimeoutTask " + timeout);
					}
					break;
				} catch (Throwable t) {
					logger.error(t);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
			}
		}
	}

	public DefaultKeepAlivePolicy(PimClient client, DefaultKeepAlivePolicyOptions options) {
		this.client = client;
		this.options = options;
		this.logger = client.getOptions().getLogger();
	}

	@Override
	public void onReadPacket(Packet packet) {
		lastReadTime = System.currentTimeMillis();
	}

	@Override
	public void onWritePacket(Packet packet) {

	}

	@Override
	public void onConnected() {
		lastReadTime = System.currentTimeMillis();
		scheduleTimeoutTask(options.readTimeout, true);
	}

	@Override
	public void onDisconnected(DisconnectInfo disconnectInfo) {
		synchronized (LOCK) {
			info("stop timeout task");
			stopped = true;
			if (currentTask != null) {
				currentTask.cancel();
				currentTask = null;
			}
			timer.cancel();
			timer = null;
			// reset vars
			idleTimes = 0;
		}
	}

	public DefaultKeepAlivePolicyOptions getOptions() {
		return options;
	}

}
