package com.j256.simplezip.code;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Decoded for the DEFLATED Zip file format.
 * 
 * @author graywatson
 */
public class InflatorFileDataDecoder implements FileDataDecoder {

	private final Inflater inflater = new Inflater(true /* no wrap */);
	private InputStream delegate;
	private final byte[] tmpBuffer = new byte[10240];

	public InflatorFileDataDecoder(InputStream inputStream) throws IOException {
		this.delegate = inputStream;
		// might as well do this
		fillInflaterBuffer();
	}

	@Override
	public int decode(byte[] outputFuffer, int offset, int length) throws IOException {
		while (true) {
			try {
				int num = inflater.inflate(outputFuffer, offset, length);
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

	@Override
	public void close() throws IOException {
		inflater.end();
		delegate.close();
	}

	@Override
	public int getNumRemainingBytes() {
		return inflater.getRemaining();
	}

	@Override
	public boolean isEofReached() {
		return inflater.finished();
	}

	/**
	 * Read data from the input stream and write to the inflater to fill its buffer.
	 */
	private void fillInflaterBuffer() throws IOException {
		int num = delegate.read(tmpBuffer);
		inflater.setInput(tmpBuffer, 0, num);
	}
}
