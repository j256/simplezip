package com.j256.simplezip.code;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Stream which reads from the Zip input-stream and gives out inflated (uncompressed) bytes.
 * 
 * @author graywatson
 */
public class InflaterInputStream extends InputStream {

	private final InputStream delegate;
	private final Inflater inflater;
	private final byte[] singleByteBuffer = new byte[1];
	private final byte[] buffer = new byte[10240];

	public InflaterInputStream(InputStream delegate, Inflater inflater) throws IOException {
		this.delegate = delegate;
		this.inflater = inflater;
		// might as well do this
		fillInflaterBuffer();
	}

	@Override
	public int read() throws IOException {
		int ret = read(singleByteBuffer, 0, singleByteBuffer.length);
		if (ret < 0) {
			return -1;
		} else {
			return (int) (singleByteBuffer[0] & 0xff);
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
				throw new IOException("Inflater had data problem with zip stream", dfe);
			}
			if (inflater.finished() || inflater.needsDictionary()) {
				// I don't think the needs-dictionary should happen but that's what the Java reference code does
				return -1;
			} else if (inflater.needsInput()) {
				fillInflaterBuffer();
			}
		}
	}

	/**
	 * Read data from the input stream and write to the inflater to fill its buffer.
	 */
	private void fillInflaterBuffer() throws IOException {
		int num = delegate.read(buffer);
		inflater.setInput(buffer, 0, num);
		System.out.println("reading: " + Arrays.toString(Arrays.copyOfRange(buffer, 0, num)));
	}
}
