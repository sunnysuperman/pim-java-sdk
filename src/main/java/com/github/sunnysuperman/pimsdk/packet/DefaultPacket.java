package com.github.sunnysuperman.pimsdk.packet;

import com.github.sunnysuperman.pimsdk.Packet;

public class DefaultPacket extends Packet {

	public DefaultPacket(byte type, byte[] body) {
		super(type);
		this.body = body;
	}

	@Override
	protected byte[] makeBody() {
		return null;
	}

}
