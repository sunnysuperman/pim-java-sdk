package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;
import com.github.sunnysuperman.pimsdk.util.PimUtil;

public class MessageReceipt extends Packet {
	private String msgID;

	public MessageReceipt(String msgID) {
		super(PacketType.TYPE_MSG_RECEIPT);
		this.msgID = msgID;
	}

	public String getMsgID() {
		return msgID;
	}

	@Override
	protected byte[] makeBody() {
		return PimUtil.wrapBytes(msgID);
	}
	
	public static MessageReceipt deserialize(byte[] body) {
		MessageReceipt packet = new MessageReceipt(PimUtil.wrapString(body));
		packet.body = body;
		return packet;
	}

}
