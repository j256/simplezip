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
	private final OutputStream outputStream;
	private byte[] encodeBuffer = new byte[10240];
	private byte[] tmpBuffer = new byte[10240];

	public DeflatorFileDataEncoder(OutputStream outputStream, int level) {
		this.outputStream = outputStream;
		this.deflater = new Deflater(level, true /* no wrap */);
	}

	@Override
	public void encode(byte[] outputBuffer, int offset, int length) throws IOException {
		if (length == 0) {
			return;
		}
		// deflater is using our buffer
		if (length > encodeBuffer.length) {
			encodeBuffer = new byte[length];
		}
		System.arraycopy(outputBuffer, offset, encodeBuffer, 0, length);
		deflater.setInput(encodeBuffer, 0, length);
		emptyDeflaterBuffer();
	}

	@Override
	public void close() throws IOException {
		// finish must be called first here
		deflater.finish();
		while (!deflater.finished()) {
			emptyDeflaterBuffer();
		}
		// deflater end must be called after the close
		deflater.end();
	}

	/**
	 * Read data from the input stream and write to the inflater to fill its buffer.
	 */
	private void emptyDeflaterBuffer() throws IOException {
		while (true) {
			int num = deflater.deflate(tmpBuffer, 0, tmpBuffer.length, Deflater.NO_FLUSH);
			if (num == 0) {
				break;
			}
			outputStream.write(tmpBuffer, 0, num);
		}
	}
}
