package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
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
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Write out a Zip file to either a {@link File} or an {@link OutputStream}.
 * 
 * @author graywatson
 */
public class ZipFileWriter implements Closeable {

	private final CountingOutputStream countingOutputStream;

	private ZipFileHeader currentFileHeader;
	private FileDataEncoder fileDataEncoder;
	private CentralDirectoryEnd.Builder dirEndBuilder = CentralDirectoryEnd.builder();
	private DataDescriptor.Builder dataDescriptorBuilder = DataDescriptor.builder();
	private int fileCount = 0;

	/**
	 * Start writing a Zip-file to a file-path. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileWriter(String filePath) throws FileNotFoundException {
		this(new File(filePath));
	}

	/**
	 * Start writing a Zip-file to a file. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileWriter(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}

	/**
	 * Start writing a Zip-file to an output-stream. You must call {@link #close()} to close the stream when you are
	 * done.
	 */
	public ZipFileWriter(OutputStream outputStream) {
		this.countingOutputStream = new CountingOutputStream(outputStream);
	}

	/**
	 * Write a file-header which starts the Zip-file.
	 */
	public void writeFileHeader(ZipFileHeader fileHeader) throws IOException {
		// XXX: need to record file info for the central directory
		// also need to save the bytes so we can calc crc and size up front if wanted
		fileHeader.write(countingOutputStream);
		currentFileHeader = fileHeader;
		countingOutputStream.resetFileInfo();
		fileCount++;
	}

	/**
	 * Write the contents of a file to the Zip-file stream. Must be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 */
	public void writeFile(String filePath) throws IOException {
		writeFile(new File(filePath));
	}

	/**
	 * Write the contents of a file to the Zip-file stream. Must be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 */
	public void writeFile(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			writeFileData(inputStream);
		}
	}

	/**
	 * Read from the input-stream and write its contents to the the Zip-file stream. Must be called after you write the
	 * file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
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
	 * Write file data as single or multiple byte arrays to the the Zip-file stream. Must be called after you write the
	 * file-header. At the end of the writing, you must call {@link #finishFileData()}.
	 */
	public void writeFileData(byte[] buffer) throws IOException {
		writeFileData(buffer, 0, buffer.length);
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream. Must be called after you write the
	 * file-header. At the end of the writing, you must call {@link #finishFileData()}.
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
	 * 
	 * NOTE: This will write a {@link DataDescriptor} at the end of the file unless a CRC32, compressed, or uncompressed
	 * size was specified in the header.
	 */
	public void finishFileData() throws IOException {
		if (fileDataEncoder == null) {
			throw new IllegalStateException("Need to call writeFileData() before you can finish");
		}
		fileDataEncoder.close();
		if (currentFileHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
			dataDescriptorBuilder.reset();
			CountingInfo fileInfo = countingOutputStream.getFileInfo();
			// XXX: need to handle zip64
			dataDescriptorBuilder.setCompressedSize((int) fileInfo.getByteCount());
			dataDescriptorBuilder.setUncompressedSize((int) fileDataEncoder.getNumBytesWritten());
			dataDescriptorBuilder.setCrc32(fileInfo.getCrc32());
			dataDescriptorBuilder.build().write(countingOutputStream);
		}
		fileDataEncoder = null;
	}

	/**
	 * Write a central-directory file-header after all of the files have been written.
	 */
	public void writeDirectoryFileHeader(CentralDirectoryFileHeader dirHeader) throws IOException {
		if (dirEndBuilder.getDirectoryOffset() == 0) {
			dirEndBuilder.setDirectoryOffset(countingOutputStream.getTotalInfo().getByteCount());
			dirEndBuilder.setNumRecordsOnDisk(fileCount);
			dirEndBuilder.setNumRecordsTotal(fileCount);
			// not sure what to do with these
			dirEndBuilder.setDiskNumber(1);
			dirEndBuilder.setDiskNumberStart(1);
		}
		dirHeader.write(countingOutputStream);
	}

	/**
	 * Write a central-directory end at the end of the Zip file.
	 */
	public void writeDirectoryEnd() throws IOException {
		writeDirectoryEnd((byte[]) null);
	}

	/**
	 * Write a central-directory end at the end of the Zip-file with a comment.
	 */
	public void writeDirectoryEnd(String comment) throws IOException {
		writeDirectoryEnd(comment.getBytes());
	}

	/**
	 * Write a central-directory end at the end of the Zip-file with a comment.
	 */
	public void writeDirectoryEnd(byte[] commentBytes) throws IOException {
		dirEndBuilder.setCommentBytes(commentBytes);
		long startOffset = dirEndBuilder.getDirectoryOffset();
		// XXX: should be long? and need to handle zip64
		int size = (int) (countingOutputStream.getTotalInfo().getByteCount() - startOffset);
		dirEndBuilder.setDirectorySize(size);
		dirEndBuilder.build().write(countingOutputStream);
	}

	/**
	 * Flush the associated output stream.
	 */
	public void flush() throws IOException {
		countingOutputStream.flush();
	}

	/**
	 * Close the associated output stream.
	 */
	@Override
	public void close() throws IOException {
		countingOutputStream.close();
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
		fileDataEncoder.registerOutputStream(countingOutputStream);
	}
}
