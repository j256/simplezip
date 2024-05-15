package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.j256.simplezip.code.DeflatorFileDataEncoder;
import com.j256.simplezip.code.FileDataEncoder;
import com.j256.simplezip.code.StoredFileDataEncoder;
import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CentralDirectoryFileInfo;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Write out a Zip-file to either a {@link File} or an {@link OutputStream}.
 * 
 * @author graywatson
 */
public class ZipFileWriter implements Closeable {

	private final CountingOutputStream countingOutputStream;
	private final CountingInfo incomingFileDateInfo = new CountingInfo();

	private ZipFileHeader currentFileHeader;
	private FileDataEncoder fileDataEncoder;
	private DataDescriptor.Builder dataDescriptorBuilder = DataDescriptor.builder();
	private CentralDirectoryFileHeader.Builder dirFileBuilder = CentralDirectoryFileHeader.builder();
	private List<CentralDirectoryFileHeader> dirFileHeaders = new ArrayList<>();
	private boolean fileFinished = true;
	private boolean zipFinished;
	private long fileStartOffset;
	private int fileCount;

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
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileHeader(ZipFileHeader fileHeader) throws IOException {
		if (zipFinished) {
			throw new IllegalStateException("Cannot write another file-header if the zip has been finished");
		}
		if (!fileFinished) {
			throw new IllegalStateException("Need to call finishFileData() before writing the next file-header");
		}
		// XXX: need to record file info for the central directory
		// also need to save the bytes so we can calc crc and size up front if wanted
		fileHeader.write(countingOutputStream);
		currentFileHeader = fileHeader;
		incomingFileDateInfo.reset();
		dirFileBuilder.reset();
		dirFileBuilder.setFileHeader(fileHeader);
		fileFinished = false;
		fileCount++;
		return countingOutputStream.getByteCount();
	}

	/**
	 * Add additional information (most optional_ to the Zip central-directory file-header that is written to the end of
	 * the Zip-file and which holds information about the files that is not contained in the {@link ZipFileHeader}.
	 */
	public void addDirectoryFileInfo(CentralDirectoryFileInfo fileInfo) {
		if (zipFinished) {
			throw new IllegalStateException("Cannot add directory file-info if the zip has been finished");
		}
		dirFileBuilder.addFileInfo(fileInfo);
	}

	/**
	 * Write the contents of a file to the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Must be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFile(String filePath) throws IOException {
		return writeFile(new File(filePath));
	}

	/**
	 * Write the contents of a file to the Zip-file stream without any encoding. Must be called after you write the
	 * file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFile(String filePath) throws IOException {
		return writeRawFile(new File(filePath));
	}

	/**
	 * Write the contents of a file to the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Must be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFile(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return writeFileData(inputStream);
		}
	}

	/**
	 * Write the contents of a file to the Zip-file stream without any encoding. Must be called after you write the
	 * file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFile(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return writeRawFileData(inputStream);
		}
	}

	/**
	 * Read from the input-stream and write its contents to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Must be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileData(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[10240];
		while (true) {
			int numRead = inputStream.read(buffer);
			if (numRead < 0) {
				break;
			}
			writeFileDataPart(buffer, 0, numRead);
		}
		return finishFileData();
	}

	/**
	 * Read from the input-stream and write its contents to the the Zip-file stream without any encoding. Must be called
	 * after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFileData(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[10240];
		while (true) {
			int numRead = inputStream.read(buffer);
			if (numRead < 0) {
				break;
			}
			writeRawFileDataPart(buffer, 0, numRead);
		}
		return finishFileData();
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Must be called after you write the file-header. At the end of the
	 * writing, you must call {@link #finishFileData()}.
	 */
	public void writeFileDataPart(byte[] buffer) throws IOException {
		writeFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream without any encoding. Must be called
	 * after you write the file-header. At the end of the writing, you must call {@link #finishFileData()}.
	 */
	public void writeRawFileDataPart(byte[] buffer) throws IOException {
		writeRawFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Must be called after you write the file-header. At the end of the
	 * writing, you must call {@link #finishFileData()}.
	 */
	public void writeFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call writeFileHeader() before you can write file data");
		}
		doWriteFileDataPart(buffer, offset, length, currentFileHeader.getCompressionMethodAsEnum());
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream without any encoding. Must be called
	 * after you write the file-header. At the end of the writing, you must call {@link #finishFileData()}.
	 */
	public void writeRawFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call writeFileHeader() before you can write file data");
		}
		doWriteFileDataPart(buffer, offset, length, CompressionMethod.NONE);
	}

	/**
	 * Called at the end of the file data. Must be called after the {@link #writeFileDataPart(byte[])} or
	 * {@link #writeFileDataPart(byte[], int, int)} methods have been called.
	 * 
	 * NOTE: This will write a {@link DataDescriptor} at the end of the file unless a CRC32, compressed, or uncompressed
	 * size was specified in the header.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishFileData() throws IOException {
		if (zipFinished) {
			throw new IllegalStateException("Cannot finish file-data if the zip has been finished");
		}
		if (fileDataEncoder == null) {
			throw new IllegalStateException("Need to call writeFileData() before you can finish");
		}
		fileDataEncoder.close();
		long encodedSize = (int) (countingOutputStream.getByteCount() - fileStartOffset);
		// set our directory file-header info
		// XXX: need to handle zip64
		dirFileBuilder.setCompressedSize((int) encodedSize);
		dirFileBuilder.setUncompressedSize((int) incomingFileDateInfo.getByteCount());
		dirFileBuilder.setCrc32(incomingFileDateInfo.getCrc32());
		// set our optional data-descriptor info
		if (currentFileHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
			dataDescriptorBuilder.reset();
			// XXX: need to handle zip64
			dataDescriptorBuilder.setCompressedSize((int) encodedSize);
			dataDescriptorBuilder.setUncompressedSize((int) incomingFileDateInfo.getByteCount());
			dataDescriptorBuilder.setCrc32(incomingFileDateInfo.getCrc32());
			DataDescriptor dataDescriptor = dataDescriptorBuilder.build();
			dataDescriptor.write(countingOutputStream);
		}
		dirFileHeaders.add(dirFileBuilder.build());
		fileDataEncoder = null;
		fileFinished = true;
		return countingOutputStream.getByteCount();
	}

	/**
	 * Finish writing the zip-file. See the {@link #finishZip(byte[])} for more information.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishZip() throws IOException {
		return finishZip((byte[]) null);
	}

	/**
	 * Finish writing the zip-file with a comment. See the {@link #finishZip(byte[])} for more information.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishZip(String comment) throws IOException {
		return finishZip(comment.getBytes());
	}

	/**
	 * Finish writing the Zip-file with a comment. This will write out the central-directory file-headers at the end of
	 * the Zip-file followed by the directory-end information. The central-directory file-headers have been accumulated
	 * from the {@link #writeFileHeader(ZipFileHeader)} and {@link #addDirectoryFileInfo(CentralDirectoryFileInfo)}
	 * methods.
	 *
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishZip(byte[] commentBytes) throws IOException {

		if (zipFinished) {
			return countingOutputStream.getByteCount();
		}
		if (!fileFinished) {
			finishFileData();
		}

		// start our directory end
		CentralDirectoryEnd.Builder dirEndBuilder = CentralDirectoryEnd.builder();
		dirEndBuilder.setDirectoryOffset(countingOutputStream.getByteCount());

		// write out our recorded central-directory file-headers
		for (CentralDirectoryFileHeader dirHeader : dirFileHeaders) {
			dirHeader.write(countingOutputStream);
		}

		// now write our directory end
		dirEndBuilder.setNumRecordsOnDisk(fileCount);
		dirEndBuilder.setNumRecordsTotal(fileCount);
		// not sure what to do with these
		dirEndBuilder.setDiskNumber(1);
		dirEndBuilder.setDiskNumberStart(1);
		dirEndBuilder.setCommentBytes(commentBytes);
		long startOffset = dirEndBuilder.getDirectoryOffset();
		// XXX: should be long? and need to handle zip64
		int size = (int) (countingOutputStream.getByteCount() - startOffset);
		dirEndBuilder.setDirectorySize(size);
		dirEndBuilder.build().write(countingOutputStream);
		zipFinished = true;
		return countingOutputStream.getByteCount();
	}

	/**
	 * Return the current number of bytes written to the output zip-file stream.
	 */
	public long getNumBytesWritten() {
		return countingOutputStream.getByteCount();
	}

	/**
	 * Flush the associated output stream.
	 */
	public void flush() throws IOException {
		countingOutputStream.flush();
	}

	/**
	 * Close the associated output stream. If {@link #finishZip()} has not been called then it will be called at this
	 * point.
	 */
	@Override
	public void close() throws IOException {
		if (!zipFinished) {
			finishZip((byte[]) null);
		}
		countingOutputStream.close();
	}

	private void doWriteFileDataPart(byte[] buffer, int offset, int length, CompressionMethod compressionMethod)
			throws IOException {
		if (zipFinished) {
			// might not be able to get here but let's be careful out there
			throw new IllegalStateException("Cannot write file-data if the zip has been finished");
		}
		if (fileDataEncoder == null) {
			assignFileDataEncoder(compressionMethod);
			fileStartOffset = countingOutputStream.getByteCount();
		}

		fileDataEncoder.encode(buffer, offset, length);
		incomingFileDateInfo.update(buffer, offset, length);
	}

	private void assignFileDataEncoder(CompressionMethod compressionMethod) {
		switch (compressionMethod) {
			case NONE:
				this.fileDataEncoder = new StoredFileDataEncoder(countingOutputStream);
				break;
			case DEFLATED:
				this.fileDataEncoder =
						new DeflatorFileDataEncoder(countingOutputStream, currentFileHeader.getCompressionLevel());
				break;
			default:
				throw new IllegalStateException(
						"Unknown compression method: " + compressionMethod + " (" + compressionMethod.getValue() + ")");
		}
	}
}
