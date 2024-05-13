package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;

import com.j256.simplezip.code.DeflatorFileDataEncoder;
import com.j256.simplezip.code.FileDataEncoder;
import com.j256.simplezip.code.StoredFileDataEncoder;
import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Write out a Zip file.
 * 
 * @author graywatson
 */
public class ZipFileWriter implements Closeable {

	private final OutputStream outputStream;
	private ZipFileHeader currentFileHeader;
	private FileDataEncoder fileDataEncoder;

	public ZipFileWriter(String path) throws FileNotFoundException {
		this(new File(path));
	}

	public ZipFileWriter(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}

	public ZipFileWriter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void writeFileHeader(ZipFileHeader fileHeader) throws IOException {
		// XXX: need to record file info for the central directory
		// also need to save the bytes so we can calc crc and size up front if wanted
		fileHeader.write(outputStream);
		currentFileHeader = fileHeader;
	}

	public void writeDataDescriptor(DataDescriptor dataDescriptor) throws IOException {
		// XXX: should write this if the header was set, etc.
		dataDescriptor.write(outputStream);
	}

	public void writeDirectoryFileHeader(CentralDirectoryFileHeader dirHeader) throws IOException {
		dirHeader.write(outputStream);
	}

	public void writeDirectoryEnd(CentralDirectoryEnd dirEnd) throws IOException {
		dirEnd.write(outputStream);
	}

	/**
	 * Write file data after you write the file-header.
	 */
	public void writeFileData(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[10240];
		while (true) {
			int numRead = inputStream.read(buffer);
			if (numRead < 0) {
				break;
			}
			writeFileData(buffer, 0, numRead);
		}
		finishFileData();
	}

	/**
	 * Write file data after you write the file-header.
	 */
	public void writeFileData(byte[] buffer) throws IOException {
		writeFileData(buffer, 0, buffer.length);
	}

	/**
	 * Write file data after you write the file-header.
	 */
	public void writeFileData(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call writeFileHeader() before you can write file data");
		}
		if (fileDataEncoder == null) {
			assignFileDataEncoder();
		}

		fileDataEncoder.encode(buffer, offset, length);
	}

	/**
	 * Called at the end of the file data. Must be called after the {@link #writeFileData(byte[])} or
	 * {@link #writeFileData(byte[], int, int)} methods have been called.
	 */
	public void finishFileData() throws IOException {
		if (fileDataEncoder == null) {
			throw new IllegalStateException("Need to call writeFileData() before you can finish");
		}
		fileDataEncoder.close();
		fileDataEncoder = null;
	}

	/**
	 * Flush the associated output stream.
	 */
	public void flush() throws IOException {
		outputStream.flush();
	}

	/**
	 * Close the associated output stream.
	 */
	@Override
	public void close() throws IOException {
		outputStream.close();
	}

	private void assignFileDataEncoder() {
		switch (currentFileHeader.getCompressionMethod()) {
			case NONE:
				this.fileDataEncoder = new StoredFileDataEncoder();
				break;
			case DEFLATED:
				// XXX: how to set the set the level
				this.fileDataEncoder = new DeflatorFileDataEncoder(Deflater.DEFAULT_COMPRESSION);
				break;
			default:
				throw new IllegalStateException(
						"Unknown compression method: " + currentFileHeader.getCompressionMethod() + " ("
								+ currentFileHeader.getCompressionMethodValue() + ")");
		}
		fileDataEncoder.registerOutputStream(outputStream);
	}
}
