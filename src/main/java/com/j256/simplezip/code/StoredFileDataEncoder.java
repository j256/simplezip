package com.j256.simplezip.code;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Decoded for the STORED (i.e. not compressed) Zip file format.
 * 
 * @author graywatson
 */
public class StoredFileDataEncoder implements FileDataEncoder {

	private OutputStream outputStream;
	private long numBytes;

	@Override
	public void registerOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void encode(byte[] inputBuffer, int offset, int length) throws IOException {
		outputStream.write(inputBuffer, offset, length);
		numBytes += length;
	}

	@Override
	public void close() {
		// no-op
	}

	@Override
	public long getNumBytesWritten() {
		return numBytes;
	}
}
