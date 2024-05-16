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

	private final BufferedOutputStream bufferedOutputStream;
	private final CountingInfo incomingFileDateInfo = new CountingInfo();
	private final byte[] tmpBuffer = new byte[IoUtils.STANDARD_BUFFER_SIZE];

	private ZipFileHeader currentFileHeader;
	private FileDataEncoder fileDataEncoder;
	private DataDescriptor.Builder dataDescriptorBuilder = DataDescriptor.builder();
	private CentralDirectoryFileHeader.Builder dirFileBuilder = CentralDirectoryFileHeader.builder();
	private List<CentralDirectoryFileHeader> dirFileHeaders = new ArrayList<>();
	private boolean fileFinished = true;
	private boolean zipFinished;
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
		this.bufferedOutputStream = new BufferedOutputStream(outputStream);
	}

	/**
	 * Attaches a buffered output stream to record the file-data _before_ writing out the file-header. This is useful
	 * because it means that the {@link ZipFileHeader} written before the file-data will contain the encoded size and
	 * the crc32 information as opposed to the {@link DataDescriptor} which is written _after_ the file-data.
	 * 
	 * @param maxSizeBuffered
	 *            Maximum number of bytes that will be stored by the buffer before it gives up and will write a
	 *            {@link DataDescriptor).
	 * @param maxSizeInMemory
	 *            Maximum number of bytes that will be stored by in memory. If there is any space above this number but
	 *            below the maxSizeBuffered then it will be written to a temporary file.
	 */
	public void enableBufferedOutput(int maxSizeBuffered, int maxSizeInMemory) {
		bufferedOutputStream.enableBuffer(maxSizeBuffered, maxSizeInMemory);
	}

	/**
	 * Write a file-header which starts the Zip-file. This actually may or may not actually write it to disk depending
	 * on buffering.
	 */
	public void writeFileHeader(ZipFileHeader fileHeader) {
		if (zipFinished) {
			throw new IllegalStateException("Cannot write another file-header if the zip has been finished");
		}
		if (!fileFinished) {
			throw new IllegalStateException("Need to call finishFileData() before writing the next file-header");
		}
		bufferedOutputStream.setFileHeader(fileHeader);
		currentFileHeader = fileHeader;
		incomingFileDateInfo.reset();
		dirFileBuilder.reset();
		fileFinished = false;
		fileCount++;
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
		while (true) {
			int numRead = inputStream.read(tmpBuffer);
			if (numRead < 0) {
				break;
			}
			writeFileDataPart(tmpBuffer, 0, numRead);
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
		while (true) {
			int numRead = inputStream.read(tmpBuffer);
			if (numRead < 0) {
				break;
			}
			writeRawFileDataPart(tmpBuffer, 0, numRead);
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
		ZipFileHeader writtenFileHeader = bufferedOutputStream.finishFileData(incomingFileDateInfo.getCrc32(),
				incomingFileDateInfo.getByteCount());
		if (writtenFileHeader == null) {
			writtenFileHeader = currentFileHeader;
		}
		// set our directory file-header info
		dirFileBuilder.setFileHeader(writtenFileHeader);
		if (writtenFileHeader != currentFileHeader) {
			dirFileBuilder.assignGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR, false);
			// XXX: need to handle zip64
			dirFileBuilder.setCompressedSize((int) bufferedOutputStream.getEncodedSize());
		}
		dirFileBuilder.setUncompressedSize((int) incomingFileDateInfo.getByteCount());
		dirFileBuilder.setCrc32(incomingFileDateInfo.getCrc32());
		// set our optional data-descriptor info
		if (writtenFileHeader.needsDataDescriptor()) {
			dataDescriptorBuilder.reset();
			// XXX: need to handle zip64
			dataDescriptorBuilder.setCompressedSize((int) bufferedOutputStream.getEncodedSize());
			dataDescriptorBuilder.setUncompressedSize((int) incomingFileDateInfo.getByteCount());
			dataDescriptorBuilder.setCrc32(incomingFileDateInfo.getCrc32());
			DataDescriptor dataDescriptor = dataDescriptorBuilder.build();
			dataDescriptor.write(bufferedOutputStream);
		}
		dirFileHeaders.add(dirFileBuilder.build());
		fileDataEncoder = null;
		fileFinished = true;
		return bufferedOutputStream.getWriteCount();
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
			return bufferedOutputStream.getWriteCount();
		}
		if (!fileFinished) {
			finishFileData();
		}

		// start our directory end
		CentralDirectoryEnd.Builder dirEndBuilder = CentralDirectoryEnd.builder();
		dirEndBuilder.setDirectoryOffset(bufferedOutputStream.getWriteCount());

		// write out our recorded central-directory file-headers
		for (CentralDirectoryFileHeader dirHeader : dirFileHeaders) {
			dirHeader.write(bufferedOutputStream);
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
		int size = (int) (bufferedOutputStream.getWriteCount() - startOffset);
		dirEndBuilder.setDirectorySize(size);
		dirEndBuilder.build().write(bufferedOutputStream);
		zipFinished = true;
		return bufferedOutputStream.getWriteCount();
	}

	/**
	 * Return the current number of bytes written to the output zip-file stream.
	 */
	public long getNumBytesWritten() {
		return bufferedOutputStream.getWriteCount();
	}

	/**
	 * Flush the associated output stream.
	 */
	public void flush() throws IOException {
		bufferedOutputStream.flush();
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
		bufferedOutputStream.close();
	}

	private void doWriteFileDataPart(byte[] buffer, int offset, int length, CompressionMethod compressionMethod)
			throws IOException {
		if (zipFinished) {
			// might not be able to get here but let's be careful out there
			throw new IllegalStateException("Cannot write file-data if the zip has been finished");
		}
		if (fileDataEncoder == null) {
			assignFileDataEncoder(compressionMethod);
		}

		fileDataEncoder.encode(buffer, offset, length);
		incomingFileDateInfo.update(buffer, offset, length);
	}

	private void assignFileDataEncoder(CompressionMethod compressionMethod) {
		switch (compressionMethod) {
			case NONE:
				this.fileDataEncoder = new StoredFileDataEncoder(bufferedOutputStream);
				break;
			case DEFLATED:
				this.fileDataEncoder =
						new DeflatorFileDataEncoder(bufferedOutputStream, currentFileHeader.getCompressionLevel());
				break;
			default:
				throw new IllegalStateException(
						"Unknown compression method: " + compressionMethod + " (" + compressionMethod.getValue() + ")");
		}
	}
}
