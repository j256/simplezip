package com.j256.simplezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.j256.simplezip.encode.FileDataDecoder;
import com.j256.simplezip.encode.InflatorFileDataDecoder;
import com.j256.simplezip.encode.StoredFileDataDecoder;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Representation of a zip-file.
 * 
 * @author graywatson
 */
public class ZipFile {

	private final RewindableInputStream countingInputStream;
	private final CountingInfo countingInfo = new CountingInfo();

	private FileDataDecoder fileDataDecoder;
	private ZipFileHeader currentHeader;
	private DataDescriptor currentDataDescriptor;
	private boolean currentFileEofReached;

	public ZipFile(String path) throws FileNotFoundException {
		this(new File(path));
	}

	public ZipFile(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	public ZipFile(InputStream inputStream) {
		this.countingInputStream = new RewindableInputStream(inputStream);
	}

	public ZipFileHeader readNextFileHeader() throws IOException {
		this.currentHeader = ZipFileHeader.read(countingInputStream);
		currentFileEofReached = false;
		currentDataDescriptor = null;
		countingInfo.reset();
		return currentHeader;
	}

	public String getCurrentFileNameAsString() {
		if (currentHeader == null) {
			return null;
		} else {
			return currentHeader.getFileName();
		}
	}

	/**
	 * Read file data from the zip stream. See {@link #readFileData(ZipFileHeader, byte[], int, int)} for more details.
	 */
	public int readFileData(byte[] buffer) throws IOException {
		return readFileData(buffer, 0, buffer.length);
	}

	/**
	 * Read file data from the zip stream.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readFileData(byte[] buffer, int offset, int length) throws IOException {
		if (currentHeader == null) {
			throw new IllegalStateException("Need to call readNextHeader() before you can read file data");
		}
		if (currentFileEofReached) {
			return -1;
		}

		if (fileDataDecoder == null) {
			assignFileDataDecoder();
		}

		// XXX: need to count to validate the size and need to calculate the crc to validate
		int result = fileDataDecoder.decode(buffer, offset, length);
		if (result > 0) {
			// update our counts for the length and crc information
			countingInfo.update(buffer, offset, result);
		} else if (result < 0) {
			/*
			 * Now that we've read all of the decoded data we need to rewind the input stream because the decoder might
			 * have read more bytes than it needed and then we need to look for the data-descriptor.
			 */
			countingInputStream.rewind(fileDataDecoder.getNumRemainingBytes());
			currentFileEofReached = true;
			if (currentHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
				currentDataDescriptor = DataDescriptor.read(countingInputStream, countingInfo);
			}
		}
		return result;
	}

	/**
	 * After all of the Zip data has been read from the stream there may be an optional data-descriptor depending on
	 * whether or not the file-header has the {@link GeneralPurposeFlag#DATA_DESCRIPTOR} flag set. If there is no
	 * descriptor then null is returned here. Once the next header is read this will return null until the end of the
	 * zip data again has been reached by the next file entry.
	 */
	public DataDescriptor getCurrentDataDescriptor() {
		return currentDataDescriptor;
	}

	/**
	 * After the header, the file bytes, and any option data-descriptor has been read, validate that the zip entry was
	 * correct based on the various different length and CRC values stored and calculated.
	 */
	public ZipStatus validatePreviousFile() {
		// XXX:
		return ZipStatus.OK;
	}

	private void assignFileDataDecoder() {
		switch (currentHeader.getCompressionMethod()) {
			case NONE:
				this.fileDataDecoder = new StoredFileDataDecoder(currentHeader.getUncompressedSize());
				break;
			case DEFLATED:
				this.fileDataDecoder = new InflatorFileDataDecoder();
				break;
			default:
				throw new IllegalStateException("Unknown compression method: " + currentHeader.getCompressionMethod()
						+ " (" + currentHeader.getCompressionMethodValue() + ")");
		}
		fileDataDecoder.registerInputStream(countingInputStream);
	}
}
