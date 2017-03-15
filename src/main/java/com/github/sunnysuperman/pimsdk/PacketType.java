package com.github.sunnysuperman.pimsdk;

public interface PacketType {
	public static final byte TYPE_CONNECT = 1;
	public static final byte TYPE_CONNECT_ACK = 2;
	public static final byte TYPE_PING = 3;
	public static final byte TYPE_PONG = 4;
	public static final byte TYPE_DISCONNECT = 5;
	public static final byte TYPE_MSG = 7;
	public static final byte TYPE_MSG_ACK = 8;
	public static final byte TYPE_MSG_RECEIPT = 9;
}
