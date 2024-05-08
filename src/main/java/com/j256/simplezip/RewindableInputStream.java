package com.j256.simplezip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Buffer that records the bytes that were read, allows us to rewind.
 */
public class RewindableInputStream extends InputStream {

	xxx need to use an internal buffer and copy stuff around;
	xxx should set up front on the zip-file and reset for each file;
			
	private final InputStream delegate;
	private byte[] lastBuffer;
	private int lastOffset;
	private int lastNumRead;
	private byte[] singleByteBuffer = new byte[1];
	private byte[] extra;
	private int extraOffset;
	private int extraMax;

	public RewindableInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read() throws IOException {
		if (extra != null) {
			if (extraOffset < extraMax) {
				return extra[extraOffset++];
			}
			extra = null;
		}
		int ret = delegate.read(singleByteBuffer, 0, singleByteBuffer.length);
		if (ret < 0) {
			return -1;
		} else {
			System.out.println("read byte: " + singleByteBuffer[0]);
			return singleByteBuffer[0];
		}
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (extra != null) {
			// NOTE: by definition, the extra stuff is not counted
			if (extraOffset < extraMax) {
				int left = extraMax - extraOffset;
				length = Math.min(length, left);
				System.arraycopy(extra, extraOffset, buffer, offset, length);
				extraOffset += length;
				return length;
			}
			extra = null;
		}
		lastBuffer = buffer;
		lastOffset = offset;
		int num = delegate.read(buffer, offset, length);
		lastNumRead = num;
		return num;
	}

	/**
	 * Rewind the buffer a certain number of bytes.
	 */
	public void rewind(int numBytes) throws IOException {
		if (numBytes < lastNumRead) {
			throw new IOException("Trying to rewind " + numBytes + " but last read only has " + lastNumRead);
		}
		int lastEnd = lastOffset + lastNumRead;
		int lastStart = lastEnd - numBytes;
		if (lastStart < lastEnd) {
			extra = Arrays.copyOfRange(lastBuffer, lastStart, lastEnd);
			extraOffset = 0;
			extraMax = numBytes;
		}
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
