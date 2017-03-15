package com.github.sunnysuperman.pimsdk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {

	private static final int BUFFER_SIZE = 2048;

	public static void compress(InputStream is, OutputStream os) throws IOException {
		GZIPOutputStream gos = new GZIPOutputStream(os);
		int count;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
			gos.write(buffer, 0, count);
		}
		gos.finish();
		gos.close();
	}

	public static byte[] compress(byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		compress(bais, baos);
		baos.close();
		bais.close();
		return baos.toByteArray();
	}

	public static void decompress(InputStream is, OutputStream os) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(is);
		int count;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((count = gis.read(buffer, 0, BUFFER_SIZE)) != -1) {
			os.write(buffer, 0, count);
		}
		gis.close();
	}

	public static byte[] decompress(byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decompress(bais, baos);
		baos.close();
		bais.close();
		return baos.toByteArray();
	}

}
