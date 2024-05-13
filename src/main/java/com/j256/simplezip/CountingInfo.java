package com.j256.simplezip;

import java.util.zip.CRC32;

/**
 * Buffer that records the bytes that were read, allows us to rewind.
 * 
 * @author graywatson
 */
public class CountingInfo {

	private long byteCount;
	private final CRC32 crc32 = new CRC32();

	/**
	 * Update the count with a single byte.
	 */
	public void update(int b) {
		byteCount++;
		crc32.update(b);
	}

	/**
	 * Update the count with a buffer of bytes.
	 */
	public void update(byte[] buffer, int offset, int length) {
		byteCount += length;
		crc32.update(buffer, offset, length);
	}

	/**
	 * Reset the count so we can count something else.
	 */
	public void reset() {
		crc32.reset();
		byteCount = 0;
	}

	/**
	 * Return how many bytes were counted.
	 */
	public long getByteCount() {
		return byteCount;
	}

	/**
	 * Return the crc of the bytes.
	 */
	public long getCrc32() {
		return crc32.getValue();
	}
}
