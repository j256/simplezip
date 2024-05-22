package com.j256.simplezip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Input/output utility methods.
 * 
 * @author graywatson
 */
public class IoUtils {

	public static int STANDARD_BUFFER_SIZE = 4096;
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
	 * Read an int in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static long readIntAsLong(InputStream input, String label) throws IOException {
		long value = readAddByte(input, label, 0, 0);
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

	/**
	 * Write a byte to the output stream.
	 */
	public static void writeByte(OutputStream output, int value) throws IOException {
		output.write(value);
	}

	/**
	 * Write a short in little-endian to the output stream.
	 */
	public static void writeShort(OutputStream output, int value) throws IOException {
		writeByte(output, value, 0);
		writeByte(output, value, 8);
	}

	/**
	 * Write an int in little-endian to the output stream.
	 */
	public static void writeInt(OutputStream output, long value) throws IOException {
		writeByte(output, value, 0);
		writeByte(output, value, 8);
		writeByte(output, value, 16);
		writeByte(output, value, 24);
	}

	/**
	 * Write a long in little-endian to the output stream.
	 */
	public static void writeLong(OutputStream output, long value) throws IOException {
		writeByte(output, value, 0);
		writeByte(output, value, 8);
		writeByte(output, value, 16);
		writeByte(output, value, 24);
		writeByte(output, value, 32);
		writeByte(output, value, 40);
		writeByte(output, value, 48);
		writeByte(output, value, 56);
	}

	/**
	 * Write the length of bytes as a little-endian short to the output stream.
	 */
	public static void writeShortBytesLength(OutputStream output, byte[] bytes) throws IOException {
		if (bytes == null) {
			writeShort(output, 0);
		} else {
			writeShort(output, bytes.length);
		}
	}

	/**
	 * Write an array of bytes to the output stream.
	 */
	public static void writeBytes(OutputStream output, byte[] bytes) throws IOException {
		if (bytes != null && bytes.length > 0) {
			output.write(bytes);
		}
	}

	/**
	 * Copy the bytes from the input stream to the output stream.
	 */
	public static void copyStream(InputStream inputStream, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		while (true) {
			int num = inputStream.read(buffer);
			if (num < 0) {
				break;
			}
			output.write(buffer, 0, num);
		}
	}

	private static int readAddByte(InputStream input, String label, int current, int shift) throws IOException {
		int value = readByte(input, label);
		return current | (value << shift);
	}

	private static long readAddByte(InputStream input, String label, long current, long shift) throws IOException {
		long value = readByte(input, label);
		return current | (value << shift);
	}

	private static void writeByte(OutputStream output, int value, int shift) throws IOException {
		int b = ((value >> shift) & 0xFF);
		output.write(b);
	}

	private static void writeByte(OutputStream output, long value, long shift) throws IOException {
		int b = (int) ((value >> shift) & 0xFF);
		output.write(b);
	}
}
