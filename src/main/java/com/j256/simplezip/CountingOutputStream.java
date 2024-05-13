package com.j256.simplezip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Buffer that keeps around the last read bytes, allowing us to rewind the stream for a certain number of bytes.
 * 
 * @author graywatson
 */
public class CountingOutputStream extends OutputStream {

	private final CountingInfo totalInfo = new CountingInfo();
	private final CountingInfo fileInfo = new CountingInfo();
	private final OutputStream delegate;

	public CountingOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
		totalInfo.update(b);
		fileInfo.update(b);
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		delegate.write(buffer, offset, length);
		totalInfo.update(buffer, offset, length);
		fileInfo.update(buffer, offset, length);
	}

	/**
	 * Reset the per-file counts.
	 */
	public void resetFileInfo() {
		fileInfo.reset();
	}

	/**
	 * Get the total counts.
	 */
	public CountingInfo getTotalInfo() {
		return totalInfo;
	}

	/**
	 * Get the per file counts.
	 */
	public CountingInfo getFileInfo() {
		return fileInfo;
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
