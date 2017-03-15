package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;
import com.github.sunnysuperman.pimsdk.util.PimUtil;

public class MessageAck extends Packet {
	private String sequenceID;
	private String msgID;

	public MessageAck(String sequenceID, String msgID) {
		super(PacketType.TYPE_MSG_ACK);
		this.sequenceID = sequenceID;
		this.msgID = msgID;
	}

	public String getSequenceID() {
		return sequenceID;
	}

	public String getMsgID() {
		return msgID;
	}

	@Override
	protected byte[] makeBody() {
		byte[] sequenceIDBytes = PimUtil.wrapBytes(sequenceID);
		byte[] msgIDBytes = msgID == null ? null : PimUtil.wrapBytes(msgID);
		int msgIDLen = msgIDBytes != null ? msgIDBytes.length : 0;
		byte[] b = new byte[1 + sequenceIDBytes.length + 1 + msgIDLen];

		int offset = 0;
		b[offset] = (byte) sequenceIDBytes.length;
		offset++;
		System.arraycopy(sequenceIDBytes, 0, b, offset, sequenceIDBytes.length);
		offset += sequenceIDBytes.length;
		b[offset] = (byte) msgIDLen;
		if (msgIDLen > 0) {
			offset++;
			System.arraycopy(msgIDBytes, 0, b, offset, msgIDLen);
		}
		return b;
	}

	public static MessageAck deserialize(byte[] body) {
		int offset = 0;
		int sequenceIDLen = (body[offset] & 0xFF);
		offset++;
		byte[] sequenceID = PimUtil.copyOfRange(body, offset, offset + sequenceIDLen);
		offset += sequenceIDLen;
		int msgIDLen = (body[offset] & 0xFF);
		byte[] msgID = null;
		if (msgIDLen > 0) {
			offset++;
			msgID = PimUtil.copyOfRange(body, offset, offset + msgIDLen);
		}
		MessageAck ack = new MessageAck(PimUtil.wrapString(sequenceID), PimUtil.wrapString(msgID));
		ack.body = body;
		return ack;
	}

}
