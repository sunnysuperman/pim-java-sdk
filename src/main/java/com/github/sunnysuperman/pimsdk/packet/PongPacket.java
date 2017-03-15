package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;
import com.github.sunnysuperman.pimsdk.PacketType;

public class PongPacket extends Packet {

	public PongPacket() {
		super(PacketType.TYPE_PONG);
	}

	@Override
	protected byte[] makeBody() {
		return null;
	}
}
