package com.j256.simplezip.code;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * For decoding the zip file data.
 * 
 * @author graywatson
 */
public interface FileDataDecoder extends Closeable {

	/**
	 * Register our streams with the decoder.
	 */
	public void registerInputStream(InputStream inputStream) throws IOException;

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

	/**
	 * Return the number of left over bytes in the last read buffer.
	 */
	public int getNumRemainingBytes();

	/**
	 * Has EOF been reached on the data stream.
	 */
	public boolean isEofReached();
}
