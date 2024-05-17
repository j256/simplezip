package com.j256.simplezip.code;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;

/**
 * Decoded for the DEFLATED Zip file format.
 * 
 * @author graywatson
 */
public class InflatorFileDataDecoder implements FileDataDecoder {

	private final Inflater inflater = new Inflater(true /* no wrap */);
	private RewindableInputStream delegate;
	/**
	 * We need to not read too much ahead because otherwise we run the risk of reading off the end of an inner zip file
	 * and not be able to rewind back enough. So we need to use a temporary buffer which is readed the deflated bytes
	 * that is a maximum of a CentralDirectoryFileHeader and a CentralDirectoryEnd.
	 */
	private final byte[] tmpBuffer =
			new byte[CentralDirectoryFileHeader.MINIMUM_READ_SIZE + CentralDirectoryEnd.MINIMUM_READ_SIZE];

	public InflatorFileDataDecoder(RewindableInputStream inputStream) throws IOException {
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
				/*
				 * Now that we've read all of the decoded data we need to rewind the input stream because the decoder
				 * might have read more bytes than it needed and we need to rewind to the start of the data-descriptor
				 * or the next record.
				 */
				delegate.rewind(inflater.getRemaining());
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

	/**
	 * Read data from the input stream and write to the inflater to fill its buffer.
	 */
	private void fillInflaterBuffer() throws IOException {
		int num = delegate.read(tmpBuffer);
		if (num > 0) {
			inflater.setInput(tmpBuffer, 0, num);
		}
	}
}
