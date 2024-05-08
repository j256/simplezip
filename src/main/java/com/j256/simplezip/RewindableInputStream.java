package com.j256.simplezip;

import java.io.IOException;
import java.io.InputStream;

/**
 * Buffer that keeps around the last read bytes, allowing us to rewind the stream for a certain number of bytes.
 */
public class RewindableInputStream extends InputStream {

	private final InputStream delegate;
	private byte[] buffer;
	private int offset;
	private int extraOffset;

	public RewindableInputStream(InputStream delegate, int bufSize) {
		this.delegate = delegate;
		this.buffer = new byte[bufSize];
	}

	@Override
	public int read() throws IOException {
		if (extraOffset < offset) {
			int ret = buffer[extraOffset++];
			return ret;
		}
		ensureSpace(1);
		int ret = delegate.read(buffer, offset, 1);
		if (ret < 0) {
			return -1;
		}
		ret = (int)(buffer[offset++] & 0xFF);
		extraOffset++;
		return ret;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] outBuffer, int outOffset, int length) throws IOException {
		if (length == 0) {
			return 0;
		}
		int extraRead = 0;
		while (extraOffset < offset && length > 0) {
			outBuffer[outOffset++] = buffer[extraOffset++];
			length--;
			extraRead++;
		}
		if (length == 0) {
			return extraRead;
		}
		ensureSpace(length);
		int numRead = delegate.read(buffer, offset, length);
		if (numRead < 0) {
			if (extraRead == 0) {
				return -1;
			} else {
				return extraRead;
			}
		}
		System.arraycopy(buffer, offset, outBuffer, outOffset, numRead);
		offset += numRead;
		extraOffset += numRead;
		return extraRead + numRead;
	}

	/**
	 * Rewind the buffer a certain number of bytes.
	 */
	public void rewind(int numBytes) throws IOException {
		if (numBytes > offset) {
			throw new IOException("Trying to rewind " + numBytes + " but buffer only has " + offset);
		}
		extraOffset = offset - numBytes;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	private void ensureSpace(int numBytes) {
		if (offset + numBytes <= buffer.length) {
			return;
		}
		if (numBytes > buffer.length) {
			int newLength = Math.max(buffer.length * 2, numBytes * 2);
			buffer = new byte[newLength];
		}
		offset = 0;
		extraOffset = 0;
	}
}
