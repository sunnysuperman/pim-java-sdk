package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;

public class ConnectAckPacket extends Packet {

	public ConnectAckPacket(byte[] info) {
		super(PacketType.TYPE_CONNECT_ACK);
		this.body = info;
	}

	@Override
	protected byte[] makeBody() {
		return null;
	}

}
