package com.github.sunnysuperman.pimsdk.packet;

import java.util.Date;

import com.github.sunnysuperman.pimsdk.ClientID;
import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;
import com.github.sunnysuperman.pimsdk.util.PimUtil;

public class Message extends Packet implements Cloneable {
	ClientID to;
	ClientID from;
	Date time;
	String sequenceID;
	String msgID;
	byte[] extra;
	byte[] content;
	String contentAsString;

	public Message() {
		super(PacketType.TYPE_MSG);
	}

	public Message(byte type) {
		super(type);
	}

	public final ClientID getTo() {
		return to;
	}

	public final void setTo(ClientID to) {
		this.to = to;
		clearBody();
	}

	public final ClientID getFrom() {
		return from;
	}

	public final void setFrom(ClientID from) {
		this.from = from;
		clearBody();
	}

	public final Date getTime() {
		return time;
	}

	public final void setTime(Date time) {
		this.time = time;
		clearBody();
	}

	public final String getSequenceID() {
		return sequenceID;
	}

	public final void setSequenceID(String sequenceID) {
		this.sequenceID = sequenceID;
		clearBody();
	}

	public final String getMsgID() {
		return msgID;
	}

	public final void setMsgID(String msgID) {
		this.msgID = msgID;
		clearBody();
	}

	public final byte[] getExtra() {
		return extra;
	}

	public final void setExtra(byte[] extra) {
		this.extra = extra;
		clearBody();
	}

	public final byte[] getContent() {
		return content;
	}

	public final void setContent(byte[] content) {
		this.content = content;
		this.contentAsString = null;
		clearBody();
	}

	public final String getContentAsString() {
		if (contentAsString != null) {
			return contentAsString;
		}
		byte[] content = getContent();
		if (content == null) {
			return null;
		}
		contentAsString = PimUtil.wrapString(content);
		return contentAsString;
	}

	public void setContentString(String contentAsString) {
		this.content = PimUtil.wrapBytes(contentAsString);
		this.contentAsString = contentAsString;
		clearBody();
	}

	public final int getContentSize() {
		return content == null ? 0 : content.length;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Message deserialize(byte[] body) {
		return deserialize(body, new Message());
	}

	public static <T extends Message> T deserialize(byte[] body, T msg) {
		if (body == null) {
			return null;
		}
		int len = body.length;
		if (len == 0) {
			return null;
		}
		byte metadata = body[0];
		int offset = 1;
		// 00EIMSFT
		for (int i = 0; i <= 5; i++) {
			byte has = (byte) ((metadata >> i) & 0x1);
			if (has == 0) {
				continue;
			}
			int dataSize = 0;
			if (i == 5) {
				if (offset + 1 >= len) {
					return null;
				}
				dataSize = (body[offset + 1] & 0xFF) + (body[offset] & 0xFF) * 256;
				offset += 2;
			} else if (i == 4) {
				dataSize = 8;
			} else {
				if (offset >= len) {
					return null;
				}
				dataSize = body[offset] & 0xFF;
				offset++;
			}
			if (dataSize == 0) {
				continue;
			}
			byte[] data = PimUtil.copyOfRange(body, offset, offset + dataSize);
			if (data == null) {
				return null;
			}
			offset += dataSize;
			if (i == 0) {
				msg.to = ClientID.wrap(PimUtil.wrapString(data));
			} else if (i == 1) {
				msg.from = ClientID.wrap(PimUtil.wrapString(data));
			} else if (i == 2) {
				msg.sequenceID = PimUtil.wrapString(data);
			} else if (i == 3) {
				msg.msgID = PimUtil.wrapString(data);
			} else if (i == 4) {
				long t = PimUtil.bytes2long(data);
				msg.time = new Date(t);
			} else if (i == 5) {
				msg.extra = data;
			}
		}
		if (offset < len) {
			byte[] content = new byte[len - offset];
			System.arraycopy(body, offset, content, 0, content.length);
			msg.content = content;
		}
		msg.contentAsString = null;
		msg.body = body;
		return msg;
	}

	public Message clone() throws CloneNotSupportedException {
		return (Message) super.clone();
	}

	@Override
	protected byte[] makeBody() {
		byte metadata = 0;
		// 00EIMSFT
		final byte flag = 0x1;
		int len = 1;

		byte[] toBytes = to == null ? null : PimUtil.wrapBytes(to.toString());
		if (toBytes != null) {
			metadata += (flag);
			len += (1 + toBytes.length);
		}

		byte[] fromBytes = from == null ? null : PimUtil.wrapBytes(from.toString());
		if (fromBytes != null) {
			metadata += (flag << 1);
			len += (1 + fromBytes.length);
		}

		byte[] sequenceIDBytes = sequenceID == null ? null : PimUtil.wrapBytes(sequenceID);
		if (sequenceIDBytes != null) {
			metadata += (flag << 2);
			len += (1 + sequenceIDBytes.length);
		}

		byte[] msgIDBytes = msgID == null ? null : PimUtil.wrapBytes(msgID);
		if (msgIDBytes != null) {
			metadata += (flag << 3);
			len += (1 + msgIDBytes.length);
		}

		byte[] timeBytes = time == null ? null : PimUtil.long2bytes(time.getTime());
		if (timeBytes != null) {
			metadata += (flag << 4);
			len += (timeBytes.length);
		}

		byte[] extraBytes = extra;
		if (extraBytes != null) {
			metadata += (flag << 5);
			len += (2 + extraBytes.length);
		}

		int contentLen = content == null ? 0 : content.length;
		len += contentLen;

		byte[] body = new byte[len];
		int offset = 0;
		body[offset] = metadata;
		offset++;

		if (toBytes != null) {
			body[offset] = (byte) toBytes.length;
			offset++;
			System.arraycopy(toBytes, 0, body, offset, toBytes.length);
			offset += toBytes.length;
		}

		if (fromBytes != null) {
			body[offset] = (byte) fromBytes.length;
			offset++;
			System.arraycopy(fromBytes, 0, body, offset, fromBytes.length);
			offset += fromBytes.length;
		}

		if (sequenceIDBytes != null) {
			body[offset] = (byte) sequenceIDBytes.length;
			offset++;
			System.arraycopy(sequenceIDBytes, 0, body, offset, sequenceIDBytes.length);
			offset += sequenceIDBytes.length;
		}

		if (msgIDBytes != null) {
			body[offset] = (byte) msgIDBytes.length;
			offset++;
			System.arraycopy(msgIDBytes, 0, body, offset, msgIDBytes.length);
			offset += msgIDBytes.length;
		}

		if (timeBytes != null) {
			System.arraycopy(timeBytes, 0, body, offset, timeBytes.length);
			offset += timeBytes.length;
		}

		if (extraBytes != null) {
			int extraLen = extraBytes.length;
			byte b1 = (byte) (extraLen & 0x00ff);
			extraLen >>= 8;
			byte b2 = (byte) (extraLen & 0x00ff);
			body[offset] = b2;
			body[offset + 1] = b1;
			offset += 2;
			System.arraycopy(extraBytes, 0, body, offset, extraBytes.length);
			offset += extraBytes.length;
		}

		if (contentLen > 0) {
			System.arraycopy(content, 0, body, offset, contentLen);
		}

		return body;
	}

}
