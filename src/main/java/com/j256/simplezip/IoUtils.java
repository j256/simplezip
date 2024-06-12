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
	public static int MAX_UNSIGNED_SHORT_VALUE = 65535;
	public static long MAX_UNSIGNED_INT_VALUE = 4294967295L;
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
	public static int readShort(InputStream input, byte[] bytes, String label) throws IOException {
		readFully(input, 2, bytes, label);
		return ((int) bytes[0] & 0xFF) | (((int) bytes[1] & 0xFF) << 8);
	}

	/**
	 * Read an int in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static int readInt(InputStream input, byte[] bytes, String label) throws IOException {
		readFully(input, 4, bytes, label);
		return ((int) bytes[0] & 0xFF) //
				| (((int) bytes[1] & 0xFF) << 8) //
				| (((int) bytes[2] & 0xFF) << 16) //
				| (((int) bytes[3] & 0xFF) << 24);
	}

	/**
	 * Read an int in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static long readIntAsLong(InputStream input, byte[] bytes, String label) throws IOException {
		readFully(input, 4, bytes, label);
		return ((long) bytes[0] & 0xFF) //
				| (((long) bytes[1] & 0xFF) << 8) //
				| (((long) bytes[2] & 0xFF) << 16) //
				| (((long) bytes[3] & 0xFF) << 24);
	}

	/**
	 * Read a long in little-endian from the input stream throwing EOFException if end is reached.
	 */
	public static long readLong(InputStream input, byte[] bytes, String label) throws IOException {
		readFully(input, 8, bytes, label);
		return ((long) bytes[0] & 0xFF) //
				| (((long) bytes[1] & 0xFF) << 8) //
				| (((long) bytes[2] & 0xFF) << 16) //
				| (((long) bytes[3] & 0xFF) << 24) //
				| (((long) bytes[4] & 0xFF) << 32) //
				| (((long) bytes[5] & 0xFF) << 40) //
				| (((long) bytes[6] & 0xFF) << 48) //
				| (((long) bytes[7] & 0xFF) << 56);
	}

	/**
	 * Read an array of bytes from the input stream throwing EOFException if end is reached.
	 */
	public static byte[] readBytes(InputStream input, int size, String label) throws IOException {
		if (size == 0) {
			return NO_BYTES;
		} else {
			return readFully(input, size, label);
		}
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
	public static void writeShort(OutputStream output, byte[] bytes, int value) throws IOException {
		bytes[0] = (byte) ((value >> 0) & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		output.write(bytes, 0, 2);
	}

	/**
	 * Write an int in little-endian to the output stream.
	 */
	public static void writeInt(OutputStream output, byte[] bytes, long value) throws IOException {
		bytes[0] = (byte) ((value >> 0) & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
		bytes[3] = (byte) ((value >> 24) & 0xFF);
		output.write(bytes, 0, 4);
	}

	/**
	 * Write a long in little-endian to the output stream.
	 */
	public static void writeLong(OutputStream output, byte[] bytes, long value) throws IOException {
		bytes[0] = (byte) ((value >> 0) & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
		bytes[3] = (byte) ((value >> 24) & 0xFF);
		bytes[4] = (byte) ((value >> 32) & 0xFF);
		bytes[5] = (byte) ((value >> 40) & 0xFF);
		bytes[6] = (byte) ((value >> 48) & 0xFF);
		bytes[7] = (byte) ((value >> 56) & 0xFF);
		output.write(bytes, 0, 8);
	}

	/**
	 * Write the length of bytes as a little-endian short to the output stream.
	 */
	public static void writeShortBytesLength(OutputStream output, byte[] tmpBytes, byte[] bytes) throws IOException {
		if (bytes == null) {
			writeShort(output, tmpBytes, 0);
		} else {
			writeShort(output, tmpBytes, bytes.length);
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

	private static byte[] readFully(InputStream input, int length, String label) throws IOException {
		byte[] bytes = new byte[length];
		int offset = 0;
		while (length > 0) {
			int numRead = input.read(bytes, offset, length);
			if (numRead < 0) {
				throw new EOFException("reached unexpected EOF while reading " + length + " bytes for " + label);
			}
			length -= numRead;
			offset += numRead;
		}
		return bytes;
	}

	private static void readFully(InputStream input, int length, byte[] bytes, String label) throws IOException {
		int offset = 0;
		while (length > 0) {
			int numRead = input.read(bytes, offset, length);
			if (numRead < 0) {
				throw new EOFException("reached unexpected EOF while reading " + length + " bytes for " + label);
			}
			length -= numRead;
			offset += numRead;
		}
	}
}
