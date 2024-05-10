package com.j256.simplezip.encode;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * For decoding the zip file data.
 * 
 * @author graywatson
 */
public interface FileDataDecoder extends Closeable {

	/**
	 * Register our streams with the decoder.
	 * 
	 * @param inputStream
	 *            Stream from which we will be reading encoded bytes.
	 */
	public void registerInputStream(InputStream inputStream) throws IOException;

	/**
	 * Decode a buffer bytes from a Zip file.
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
	 * Decode a buffer bytes from a Zip file.
	 * 
	 * @param outputBuffer
	 *            Buffer to write decoded bytes into.
	 * @return The number of bytes written into the outputBlock at the outputOffset.
	 */
	public int decode(byte[] outputBuffer) throws IOException;

	/**
	 * Return the number of left over bytes in the last read buffer.
	 */
	public int getNumRemainingBytes();

	/**
	 * Has EOF been reached on the data stream.
	 */
	public boolean isEofReached();

	/**
	 * Visit a buffer of bytes that had been decoded and is to be written to the output stream in
	 * {@link FileDataDecoder#decode(InputStream, OutputStream)}. from the input to the output.
	 */
	public static interface BufferVisitor {
		/**
		 * While the bytes are being decoded, this is called to allow the caller to see the bytes passing through the
		 * streams.
		 * 
		 * @param block
		 *            Block that was visited
		 * @param offset
		 *            offset in block where the new bytes were decoded
		 * @param length
		 *            length of the new bytes that were decoded
		 */
		public void visit(byte[] block, int offset, int length);
	}
}
