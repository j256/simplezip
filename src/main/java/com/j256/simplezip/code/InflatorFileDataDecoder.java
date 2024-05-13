package com.j256.simplezip.code;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;

/**
 * Decoded for the DEFLATED Zip file format.
 * 
 * @author graywatson
 */
public class InflatorFileDataDecoder implements FileDataDecoder {

	private final Inflater inflater = new Inflater(true /* no wrap */);
	private InflaterInputStream inflaterInputStream;

	@Override
	public void registerInputStream(InputStream inputStream) throws IOException {
		this.inflaterInputStream = new InflaterInputStream(inputStream, this.inflater);
	}

	@Override
	public int decode(byte[] outputFuffer, int offset, int length) throws IOException {
		int numRead = inflaterInputStream.read(outputFuffer, offset, length);
		if (numRead < 0) {
			return -1;
		} else {
			return numRead;
		}
	}

	@Override
	public void close() throws IOException {
		inflater.end();
		inflaterInputStream.close();
	}

	@Override
	public int getNumRemainingBytes() {
		return inflater.getRemaining();
	}

	@Override
	public boolean isEofReached() {
		return inflater.finished();
	}
}
