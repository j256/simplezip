package com.j256.simplezip;

import java.util.zip.CRC32;

/**
 * Buffer that records the bytes that were read, allows us to rewind.
 */
public class CountingInfo {

	private long byteCount;
	private final CRC32 crc32 = new CRC32();

	public void update(byte[] buffer, int offset, int length) {
		byteCount += length;
		crc32.update(buffer, offset, length);
	}

	public void reset() {
		crc32.reset();
		byteCount = 0;
	}

	public long getByteCount() {
		return byteCount;
	}

	public long getCrc32() {
		return crc32.getValue();
	}
}
