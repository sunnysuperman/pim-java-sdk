package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;

public class ConnectPacket extends Packet {

	public ConnectPacket(byte[] info) {
		super(PacketType.TYPE_CONNECT);
		this.body = info;
	}

	@Override
	protected byte[] makeBody() {
		return null;
	}

}
