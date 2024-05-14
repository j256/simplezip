package com.j256.simplezip.code;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * Decoded for the DEFLATED Zip file format.
 * 
 * @author graywatson
 */
public class DeflatorFileDataEncoder implements FileDataEncoder {

	private final Deflater deflater;
	private DeflaterOutputStream enflaterOutputStream;

	public DeflatorFileDataEncoder(int level) {
		deflater = new Deflater(level, true /* no wrap */);
	}

	@Override
	public void registerOutputStream(OutputStream outputStream) {
		this.enflaterOutputStream = new DeflaterOutputStream(outputStream, deflater);
	}

	@Override
	public void encode(byte[] outputBuffer, int offset, int length) throws IOException {
		enflaterOutputStream.write(outputBuffer, offset, length);
	}

	@Override
	public void close() throws IOException {
		enflaterOutputStream.close();
		// deflater end must be called after the close
		deflater.end();
	}
}
