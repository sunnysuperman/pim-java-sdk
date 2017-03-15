package com.github.sunnysuperman.pimsdk;

public abstract class Packet {
	private final byte type;
	protected volatile byte[] body;

	public Packet(byte type) {
		this.type = type;
	}

	public final byte getType() {
		return type;
	}

	public final byte[] getBody() {
		if (body == null) {
			body = makeBody();
		}
		return body;
	}

	public final Packet marshall() {
		getBody();
		return this;
	}

	protected final void clearBody() {
		this.body = null;
	}

	protected abstract byte[] makeBody();

}
