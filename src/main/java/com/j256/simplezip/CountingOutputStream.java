package com.j256.simplezip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Delegating output-stream which keeps a count of the bytes written.
 * 
 * @author graywatson
 */
public class CountingOutputStream extends OutputStream {

	private final OutputStream delegate;
	private long byteCount;

	public CountingOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
		byteCount++;
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		delegate.write(buffer, offset, length);
		byteCount += length;
	}

	/**
	 * Get the total counts.
	 */
	public long getTotalByteCount() {
		return byteCount;
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
