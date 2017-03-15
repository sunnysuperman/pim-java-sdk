package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;

public class PingPacket extends Packet {

	public PingPacket() {
		super(PacketType.TYPE_PING);
	}

	@Override
	protected byte[] makeBody() {
		return null;
	}
}
