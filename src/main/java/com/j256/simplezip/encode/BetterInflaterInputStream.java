package com.j256.simplezip.encode;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Re-implementation of an inflator input stream which tracks the read bytes so we can rewind if necessary.
 * 
 * @author graywatson
 */
public class BetterInflaterInputStream extends InputStream {

	private InputStream delegate;
	private final Inflater inflater;
	private final byte[] singleByteBuffer = new byte[1];
	private int remaining;

	public BetterInflaterInputStream(InputStream delegate, Inflater inflater) {
		this.delegate = delegate;
		this.inflater = inflater;
	}

	@Override
	public int read() throws IOException {
		int ret = delegate.read(singleByteBuffer, 0, singleByteBuffer.length);
		if (ret < 0) {
			return -1;
		} else {
			return singleByteBuffer[0];
		}
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		return read(bytes, 0, bytes.length);
	}

	@Override
	public int read(byte[] bytes, int offset, int len) throws IOException {
		while (true) {
			try {
				int num = inflater.inflate(bytes, offset, len);
				if (num > 0) {
					return num;
				} else {
					// 0 means that it is either finished or needs more input
				}
			} catch (DataFormatException dfe) {
				throw new IOException("Inflater saw data problem with zip stream", dfe);
			}
			if (inflater.finished()) {
				remaining = inflater.getRemaining();
				return -1;
			} else if (inflater.needsInput()) {
				int num = delegate.read(bytes);
				inflater.setInput(bytes, 0, num);
			}
		}
	}

	/**
	 * What to call at the end of the inflater process that are left over from the inflater reading.
	 */
	public int getNumRemainingBytes() {
		return remaining;
	}
}
