package com.j256.simplezip.code;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * Stream which reads from the Zip input-stream and gives out inflated (uncompressed) bytes.
 * 
 * @author graywatson
 */
public class DeflaterOutputStream extends OutputStream {

	private final OutputStream delegate;
	private final Deflater deflater;
	private final byte[] singleByteBuffer = new byte[1];
	private byte[] buffer = new byte[10240];
	private byte[] writeBuffer = new byte[10240];

	public DeflaterOutputStream(OutputStream delegate, Deflater deflater) {
		this.delegate = delegate;
		this.deflater = deflater;
	}

	@Override
	public void write(int b) throws IOException {
		singleByteBuffer[0] = (byte) b;
		write(singleByteBuffer, 0, 1);
	}

	@Override
	public void write(byte[] output, int offset, int length) throws IOException {
		if (length == 0) {
			return;
		}
		// we have to wait for this because the deflater is using our buffer
		if (length > buffer.length) {
			buffer = new byte[length];
		}
		System.arraycopy(output, offset, buffer, 0, length);
		deflater.setInput(buffer, 0, length);
		emptyDeflaterBuffer();
	}

	@Override
	public void close() throws IOException {
		// finish must be called first here
		deflater.finish();
		while (!deflater.finished()) {
			emptyDeflaterBuffer();
		}
		// NOTE: we don't close the output-buffer
	}

	/**
	 * Read data from the input stream and write to the inflater to fill its buffer.
	 */
	private void emptyDeflaterBuffer() throws IOException {
		while (true) {
			int num = deflater.deflate(writeBuffer, 0, writeBuffer.length, Deflater.NO_FLUSH);
			if (num == 0) {
				break;
			}
			delegate.write(writeBuffer, 0, num);
		}
	}
}
