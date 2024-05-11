package com.j256.simplezip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input/output utility methods.
 * 
 * @author graywatson
 */
public class IoUtils {

	private static final byte[] NO_BYTES = new byte[0];

	/**
	 * Read a byte from the input stream throwing EOFException if end is reached.
	 */
	public static int readByte(InputStream input, String label) throws IOException, EOFException {
		int value = input.read();
		if (value < 0) {
			throw new EOFException("reached unexpected EOF while reading " + label);
		} else {
			return value;
		}
	}

	/**
	 * Read a short in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static int readShort(InputStream input, String label) throws IOException {
		int value = readAddByte(input, label, 0, 0);
		value = readAddByte(input, label, value, 8);
		return value;
	}

	/**
	 * Read an int in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static int readInt(InputStream input, String label) throws IOException {
		int value = readAddByte(input, label, 0, 0);
		value = readAddByte(input, label, value, 8);
		value = readAddByte(input, label, value, 16);
		value = readAddByte(input, label, value, 24);
		return value;
	}

	/**
	 * Read a long in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static long readLong(InputStream input, String label) throws IOException {
		long value = readAddByte(input, label, 0, 0);
		value = readAddByte(input, label, value, 8);
		value = readAddByte(input, label, value, 16);
		value = readAddByte(input, label, value, 24);
		value = readAddByte(input, label, value, 32);
		value = readAddByte(input, label, value, 40);
		value = readAddByte(input, label, value, 48);
		value = readAddByte(input, label, value, 56);
		return value;
	}

	/**
	 * Read an array of bytes from the input stream throwing EOFException if end is reached.
	 */
	public static byte[] readBytes(InputStream input, int size, String label) throws IOException {
		if (size == 0) {
			return NO_BYTES;
		}
		byte[] bytes = new byte[size];
		int left = size;
		int offset = 0;
		while (left > 0) {
			int num = input.read(bytes, offset, left);
			if (num < 0) {
				throw new EOFException("reached unexpected EOF while reading " + size + " bytes of " + label);
			}
			left -= num;
			offset += num;
		}
		return bytes;
	}

	private static int readAddByte(InputStream input, String label, int current, int shift) throws IOException {
		int value = readByte(input, label);
		return current | (value << shift);
	}

	private static long readAddByte(InputStream input, String label, long current, int shift) throws IOException {
		long value = readByte(input, label);
		return current | (value << shift);
	}
}
