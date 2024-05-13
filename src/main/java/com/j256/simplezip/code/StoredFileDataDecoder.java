package com.j256.simplezip.code;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decoded for the STORED (i.e. not compressed) Zip file format.
 * 
 * @author graywatson
 */
public class StoredFileDataDecoder implements FileDataDecoder {

	private final int dataSize;

	private InputStream inputStream;
	private int inputOffset;

	public StoredFileDataDecoder(int dataSize) {
		this.dataSize = dataSize;
	}

	@Override
	public void registerInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int decode(byte[] outputBuffer, int offset, int length) throws IOException {
		int maxLength = Math.min(dataSize - inputOffset, length);
		if (maxLength <= 0) {
			// hit the end of the data
			return -1;
		}

		int numRead = inputStream.read(outputBuffer, offset, maxLength);
		if (numRead < 0) {
			return -1;
		}
		inputOffset += numRead;
		return numRead;
	}

	@Override
	public void close() {
		// no-op
	}

	@Override
	public int getNumRemainingBytes() {
		// never any remaining bytes
		return 0;
	}

	@Override
	public boolean isEofReached() {
		return (inputOffset >= dataSize);
	}
}
