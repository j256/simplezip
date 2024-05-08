package com.j256.simplezip.encode;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;

/**
 * Decoded for the DEFLATED Zip file format.
 * 
 * @author graywatson
 */
public class InflatorFileDataDecoder implements FileDataDecoder {

	private Inflater inflater;
	private BetterInflaterInputStream inflaterInputStream;

	@Override
	public void registerInputStream(InputStream inputStream) {
		this.inflater = new Inflater(true /* no wrap */);
		this.inflaterInputStream = new BetterInflaterInputStream(inputStream, this.inflater);
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
	public int decode(byte[] outputBuffer) throws IOException {
		return decode(outputBuffer, 0, outputBuffer.length);
	}

	@Override
	public void close() throws IOException {
		inflater.end();
		inflaterInputStream.close();
	}

	@Override
	public int getNumRemainingBytes() {
		return inflaterInputStream.getNumRemainingBytes();
	}
}
