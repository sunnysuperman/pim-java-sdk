package com.github.sunnysuperman.pimsdk.util;

import java.nio.charset.Charset;

public class PimUtil {
	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	public static String wrapString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return new String(bytes, CHARSET_UTF8);
	}

	public static byte[] wrapBytes(String s) {
		return s.getBytes(CHARSET_UTF8);
	}

	/**
	 * 将byte转换为一个长度为8的boolean数组（每bit代表一个boolean值）
	 */
	public static boolean[] byte2boolArray(byte b) {
		boolean[] array = new boolean[8];
		for (int i = 7; i >= 0; i--) { // 对于byte的每bit进行判定
			array[i] = (b & 1) == 1; // 判定byte的最后一位是否为1，若为1，则是true；否则是false
			b = (byte) (b >> 1); // 将byte右移一位
		}
		return array;
	}

	/**
	 * 将byte转换为一个长度为8的boolean数组（每bit代表一个boolean值）
	 */
	public static byte[] byte2bitArray(byte b) {
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--) { // 对于byte的每bit进行判定
			array[i] = (byte) (b & 1); // 判定byte的最后一位是否为1，若为1，则是true；否则是false
			b = (byte) (b >> 1); // 将byte右移一位
		}
		return array;
	}

	/**
	 * 将一个长度为8的boolean数组（每bit代表一个boolean值）转换为byte
	 */
	public static byte bitArray2byte(boolean[] array) {
		if (array == null || array.length != 8) {
			throw new RuntimeException("Bad bit array");
		}
		byte b = 0;
		for (int i = 0; i <= 7; i++) {
			if (array[i]) {
				// 0000 0001
				int nn = (1 << (7 - i));
				b += nn;
			}
		}
		return b;
	}

	public static byte getBit(byte b, byte offset) {
		return (byte) ((b >> (7 - offset)) & 0x1);
	}

	public static byte[] copyOfRange(byte[] original, int from, int to) {
		if (from >= original.length || to > original.length || to <= from) {
			return null;
		}
		int newLength = to - from;
		byte[] copy = new byte[newLength];
		System.arraycopy(original, from, copy, 0, newLength);
		return copy;
	}

	public static byte[] long2bytes(long num) {
		byte[] buf = new byte[8];
		for (int i = buf.length - 1; i >= 0; i--) {
			buf[i] = (byte) (num & 0x00000000000000ff);
			num >>= 8;
		}
		return buf;
	}

	public static long bytes2long(byte[] bytes) {
		long num = 0;
		for (int i = 0; i < 8; i++) {
			num <<= 8;
			num |= (bytes[i] & 0xff);
		}
		return num;
	}

	public final static byte[] int2bytes(int num) {
		byte[] buf = new byte[4];
		for (int i = buf.length - 1; i >= 0; i--) {
			buf[i] = (byte) (num & 0x000000ff);
			num >>= 8;
		}
		return buf;
	}

	public final static int bytes2int(byte[] bytes) {
		int num = 0;
		for (int i = 0; i < 4; i++) {
			num <<= 8;
			num |= (bytes[i] & 0x000000ff);
		}
		return num;
	}

	public static Integer parseInteger(Object s) {
		if (s == null) {
			return null;
		}
		if (s instanceof Integer) {
			return (Integer) s;
		}
		if (s instanceof Number) {
			return Integer.valueOf(((Number) s).intValue());
		}
		if (s instanceof String) {
			String theString = (String) s;
			if (theString.length() == 0) {
				return null;
			}
			return Integer.valueOf(theString);
		}
		if (s instanceof Boolean) {
			return ((Boolean) s).booleanValue() ? 1 : 0;
		}
		return null;
	}

	public static int parseIntValue(Object s, int defaultValue) {
		Integer v = parseInteger(s);
		return v == null ? defaultValue : v.intValue();
	}

}
