package com.j256.simplezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.j256.simplezip.encode.FileDataDecoder;
import com.j256.simplezip.encode.InflatorFileDataDecoder;
import com.j256.simplezip.encode.StoredFileDataDecoder;
import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Representation of a Zip file.
 * 
 * @author graywatson
 */
public class ZipFile {

	// XXX: should be maximum zip data read block
	private static final int BUFFER_SIZE = 8192;

	private final RewindableInputStream countingInputStream;
	private final CountingInfo countingInfo = new CountingInfo();

	private FileDataDecoder fileDataDecoder;
	private ZipFileHeader currentFileHeader;
	private CentralDirectoryFileHeader currentDirHeader;
	private DataDescriptor currentDataDescriptor;
	private boolean currentFileEofReached;

	public ZipFile(String path) throws FileNotFoundException {
		this(new File(path));
	}

	public ZipFile(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	public ZipFile(InputStream inputStream) {
		this.countingInputStream = new RewindableInputStream(inputStream, BUFFER_SIZE);
	}

	public ZipFileHeader readNextFileHeader() throws IOException {
		this.currentFileHeader = ZipFileHeader.read(countingInputStream);
		currentFileEofReached = false;
		currentDataDescriptor = null;
		countingInfo.reset();
		return currentFileHeader;
	}

	public CentralDirectoryFileHeader readNextDirectoryFileHeader() throws IOException {
		this.currentDirHeader = CentralDirectoryFileHeader.read(countingInputStream);
		return currentDirHeader;
	}

	public CentralDirectoryEnd readDirectoryEnd() throws IOException {
		return CentralDirectoryEnd.read(countingInputStream);
	}

	public String getCurrentFileNameAsString() {
		if (currentFileHeader == null) {
			return null;
		} else {
			return currentFileHeader.getFileName();
		}
	}

	/**
	 * Read file data from the Zip stream. See {@link #readFileData(ZipFileHeader, byte[], int, int)} for more details.
	 */
	public int readFileData(byte[] buffer) throws IOException {
		return readFileData(buffer, 0, buffer.length);
	}

	/**
	 * Read file data from the Zip stream.
	 * 
	 * NOTE: This _must_ be called until it returns -1 which indicates that EOF has been reached. Until the underlying
	 * decoders return EOF we don't know that we are done and we can't rewind over any pre-fetched bytes to continue to
	 * process the next file or the directory at the end of the Zip file.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readFileData(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call readNextHeader() before you can read file data");
		}
		if (currentFileEofReached) {
			return -1;
		}
		if (length == 0) {
			return 0;
		}

		if (fileDataDecoder == null) {
			assignFileDataDecoder();
		}

		// read in bytes from our decoder
		int result = fileDataDecoder.decode(buffer, offset, length);
		if (result > 0) {
			// update our counts for the length and crc information
			countingInfo.update(buffer, offset, result);
		}
		if (result < 0 || fileDataDecoder.isEofReached()) {
			/*
			 * Now that we've read all of the decoded data we need to rewind the input stream because the decoder might
			 * have read more bytes than it needed and we need to rewind to the start of the data-descriptor or the next
			 * record.
			 */
			countingInputStream.rewind(fileDataDecoder.getNumRemainingBytes());
			currentFileEofReached = true;
			if (currentFileHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
				currentDataDescriptor = DataDescriptor.read(countingInputStream, countingInfo);
			}
		}
		return result;
	}

	/**
	 * Returns true if the current file's data EOF has been reached. read() should have returned -1.
	 */
	public boolean isCurrentFileEofReached() {
		return currentFileEofReached;
	}

	/**
	 * After all of the Zip data has been read from the stream there may be an optional data-descriptor depending on
	 * whether or not the file-header has the {@link GeneralPurposeFlag#DATA_DESCRIPTOR} flag set. If there is no
	 * descriptor then null is returned here. Once the next header is read this will return null until the end of the
	 * Zip data again has been reached by the next file entry.
	 */
	public DataDescriptor getCurrentDataDescriptor() {
		return currentDataDescriptor;
	}

	/**
	 * After the header, the file bytes, and any option data-descriptor has been read, validate that the Zip entry was
	 * correct based on the various different length and CRC values stored and calculated.
	 */
	public ZipStatus validatePreviousFile() {
		// validate the header section and any trailing data-descriptor
		return ZipStatus.OK;
	}

	private void assignFileDataDecoder() throws IOException {
		switch (currentFileHeader.getCompressionMethod()) {
			case NONE:
				this.fileDataDecoder = new StoredFileDataDecoder(currentFileHeader.getUncompressedSize());
				break;
			case DEFLATED:
				this.fileDataDecoder = new InflatorFileDataDecoder();
				break;
			default:
				throw new IllegalStateException(
						"Unknown compression method: " + currentFileHeader.getCompressionMethod() + " ("
								+ currentFileHeader.getCompressionMethodValue() + ")");
		}
		fileDataDecoder.registerInputStream(countingInputStream);
	}
}
