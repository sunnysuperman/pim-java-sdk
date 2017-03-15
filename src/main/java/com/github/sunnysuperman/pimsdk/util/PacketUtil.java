package com.github.sunnysuperman.pimsdk.util;

public class PacketUtil {
	// first byte: 000 ? 0000

	public static byte getPacketType(byte b) {
		// byte and = 0x1;
		// byte b1 = (byte) (b & and);
		// byte b2 = (byte) ((b >> 1) & and);
		// byte b3 = (byte) ((b >> 2) & and);
		// byte b4 = (byte) ((b >> 3) & and);
		// return (byte) (b1 + b2 << 1 + b3 << 2 + b4 << 3);
		// 0000 1111
		byte flag = 15;
		return (byte) (b & flag);
	}

	private static byte setFirstByte(boolean hasData, boolean compress, byte type) {
		if (type > 15) {
			throw new RuntimeException("Bad packet type");
		}
		byte b = 0;
		// 0000 0001
		final byte flag = 0x1;

		if (hasData) {
			b += (flag << 7);
		}
		if (compress) {
			b += (flag << 6);
		}
		b += type;
		return b;
	}

	// public static byte[] makeHeader(Packet packet) {
	// byte[] body = packet.getBody();
	// int bodyLen = body == null ? 0 : body.length;
	// return makeHeader(packet.getType(), bodyLen, false);
	// }

	public static byte[] makeHeader(byte type, int bodyLen, boolean compress) {
		boolean hasData = bodyLen > 0;
		byte b = PacketUtil.setFirstByte(hasData, compress, type);
		if (hasData) {
			byte b2 = (byte) (bodyLen / 65536);
			byte b3 = (byte) ((bodyLen - b2 * 65536) / 256);
			byte b4 = (byte) (bodyLen % 256);
			byte[] header = new byte[] { b, b2, b3, b4 };
			return header;
		} else {
			return new byte[] { b };
		}
	}
}
