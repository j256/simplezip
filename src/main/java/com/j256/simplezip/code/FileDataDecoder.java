package com.j256.simplezip.code;

import java.io.Closeable;
import java.io.IOException;

/**
 * For decoding the zip file data.
 * 
 * @author graywatson
 */
public interface FileDataDecoder extends Closeable {

	/**
	 * Decode a buffer bytes from a Zip-file.
	 * 
	 * @param outputBuffer
	 *            Buffer to write decoded bytes into.
	 * @param offset
	 *            offset in the buffer to write the decoded bytes
	 * @param length
	 *            number of bytes that can be read into the buffer
	 * @return The number of bytes written into the outputBlock at the outputOffset.
	 */
	public int decode(byte[] outputBuffer, int offset, int length) throws IOException;
}
