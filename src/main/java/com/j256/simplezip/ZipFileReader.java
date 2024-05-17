package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.code.FileDataDecoder;
import com.j256.simplezip.code.InflatorFileDataDecoder;
import com.j256.simplezip.code.StoredFileDataDecoder;
import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Read in a Zip-file either from a {@link File} or an {@link InputStream}.
 * 
 * @author graywatson
 */
public class ZipFileReader implements Closeable {

	private final RewindableInputStream countingInputStream;
	private final CountingInfo countingInfo = new CountingInfo();
	private final byte[] tmpBuffer = new byte[IoUtils.STANDARD_BUFFER_SIZE];

	private FileDataDecoder fileDataDecoder;
	private ZipFileHeader currentFileHeader;
	private CentralDirectoryFileHeader currentDirHeader;
	private DataDescriptor currentDataDescriptor;
	private boolean currentFileEofReached;
	private FileDataInputStream fileDataInputStream;

	/**
	 * Start reading a Zip-file from the file-path. You must call {@link #close()} to close the stream when you are
	 * done.
	 */
	public ZipFileReader(String path) throws FileNotFoundException {
		this(new File(path));
	}

	/**
	 * Read a Zip-file from a file. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileReader(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	/**
	 * Read a Zip-file from an input-stream. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileReader(InputStream inputStream) {
		this.countingInputStream = new RewindableInputStream(inputStream, IoUtils.STANDARD_BUFFER_SIZE);
	}

	/**
	 * Read the next file header from the zip file. This is first thing that you will call.
	 */
	public ZipFileHeader readFileHeader() throws IOException {
		this.currentFileHeader = ZipFileHeader.read(countingInputStream);
		currentFileEofReached = false;
		currentDataDescriptor = null;
		fileDataDecoder = null;
		countingInfo.reset();
		return currentFileHeader;
	}

	/**
	 * Skip over the file data in the zip.
	 * 
	 * @return The number of bytes skipped.
	 */
	public long skipFileData() throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			byteCount += numRead;
		}
		return byteCount;
	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the file path argument.
	 * 
	 * @param outputPath
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileData(String outputPath) throws IOException {
		return readFileData(new FileOutputStream(outputPath));
	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the file argument.
	 * 
	 * @param outputFile
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileData(File outputFile) throws IOException {
		return readFileData(new FileOutputStream(outputFile));
	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the output-steam argument.
	 * 
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileData(OutputStream outputStream) throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			outputStream.write(tmpBuffer, 0, numRead);
			byteCount += numRead;
		}
		return byteCount;
	}

	/**
	 * Read raw file data from the Zip stream, no decoding, and write it to the output-steam argument.
	 * 
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readRawFileData(OutputStream outputStream) throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readRawFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			outputStream.write(tmpBuffer, 0, numRead);
			byteCount += numRead;
		}
		return byteCount;
	}

	/**
	 * Get an input stream suitable for reading the bytes of a single Zip file-entry. A call to the
	 * {@link InputStream#read(byte[], int, int)} basically calls through to
	 * {@link #readFileDataPart(byte[], int, int)}.
	 * 
	 * NOTE: you _must_ read from the input-stream until it returns EOF (-1).
	 * 
	 * @param raw
	 *            Set to true to make calls to {@link #readRawFileDataPart(byte[], int, int)} or false to call
	 *            {@link #readFileDataPart(byte[], int, int)}.
	 */
	public InputStream openFileDataOutputStream(boolean raw) {
		if (fileDataInputStream == null) {
			fileDataInputStream = new FileDataInputStream(raw);
		}
		return fileDataInputStream;
	}

	/**
	 * Read file data from the Zip stream and decode it into the buffer argument. See
	 * {@link #readFileDataPart(byte[], int, int)} for more details.
	 */
	public int readFileDataPart(byte[] buffer) throws IOException {
		return readFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Read raw file data from the Zip stream, without decoding, into the buffer argument. See
	 * {@link #readRawFileDataPart(byte[], int, int)} for more details.
	 */
	public int readRawFileDataPart(byte[] buffer) throws IOException {
		return readRawFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Read file data from the Zip stream and decode it into the buffer argument.
	 * 
	 * NOTE: This _must_ be called until it returns -1 which indicates that EOF has been reached. Until the underlying
	 * decoders return EOF we don't know that we are done and we can't rewind over any pre-fetched bytes to continue to
	 * process the next file or the directory at the end of the Zip file.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call readNextHeader() before you can read file data");
		}
		return doReadFileDataPart(buffer, offset, length, currentFileHeader.getCompressionMethod());
	}

	/**
	 * Read raw file data from the Zip stream, without decoding, into the buffer argument. This will only work if the
	 * compressed-size value is set in the file-header.
	 * 
	 * NOTE: This _must_ be called until it returns -1 which indicates that EOF has been reached. Until the underlying
	 * decoders return EOF we don't know that we are done and we can't rewind over any pre-fetched bytes to continue to
	 * process the next file or the directory at the end of the Zip file.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readRawFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call readNextHeader() before you can read file data");
		}
		return doReadFileDataPart(buffer, offset, length, CompressionMethod.NONE.getValue());
	}

	/**
	 * After all of the files have been read, you can read and examine the central-directory entries.
	 * 
	 * @return The next central-directory file-header or null if all entries have been read.
	 */
	public CentralDirectoryFileHeader readDirectoryFileHeader() throws IOException {
		this.currentDirHeader = CentralDirectoryFileHeader.read(countingInputStream);
		return currentDirHeader;
	}

	/**
	 * Read the central-directory end which is after all of the central-directory file-headers at the very end of the
	 * Zip file.
	 */
	public CentralDirectoryEnd readDirectoryEnd() throws IOException {
		return CentralDirectoryEnd.read(countingInputStream);
	}

	/**
	 * Close the underlying input-stream.
	 */
	@Override
	public void close() throws IOException {
		/*
		 * This is subtle. We need to read to the EOF marker in case we are in an inner Zip file. The outer decoder
		 * might need to hit the EOF so it can appropriately rewind in case it was reading ahead.
		 */
		while (true) {
			int num = countingInputStream.read(tmpBuffer);
			if (num < 0) {
				break;
			}
		}
		countingInputStream.close();
	}

	/**
	 * Return the file-name from the most recent header read.
	 */
	public String getCurrentFileNameAsString() {
		if (currentFileHeader == null) {
			return null;
		} else {
			return currentFileHeader.getFileName();
		}
	}

	/**
	 * Returns true if the current file's data EOF has been reached. read() should have returned -1.
	 */
	public boolean isFileDataEofReached() {
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

	private int doReadFileDataPart(byte[] buffer, int offset, int length, int compressionMethod) throws IOException {
		if (currentFileEofReached) {
			return -1;
		}
		if (length == 0) {
			return 0;
		}

		if (fileDataDecoder == null) {
			assignFileDataDecoder(compressionMethod);
		}

		// read in bytes from our decoder
		int result = fileDataDecoder.decode(buffer, offset, length);
		if (result >= 0) {
			// update our counts for the length and crc information
			countingInfo.update(buffer, offset, result);
		} else {
			// EOF
			fileDataDecoder.close();
			if (currentFileHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
				currentDataDescriptor = DataDescriptor.read(countingInputStream, countingInfo);
			}
			currentFileEofReached = true;
			fileDataDecoder = null;
		}
		return result;
	}

	private void assignFileDataDecoder(int compressionMethod) throws IOException {
		if (compressionMethod == CompressionMethod.NONE.getValue()) {
			this.fileDataDecoder =
					new StoredFileDataDecoder(countingInputStream, currentFileHeader.getCompressedSize());
		} else if (compressionMethod == CompressionMethod.DEFLATED.getValue()) {
			this.fileDataDecoder = new InflatorFileDataDecoder(countingInputStream);
		} else {
			throw new IllegalStateException("Unknown compression method: "
					+ CompressionMethod.fromValue(compressionMethod) + " (" + compressionMethod + ")");
		}
	}

	/**
	 * Input stream that can be used to read data for a single Zip file-entry.
	 */
	public class FileDataInputStream extends InputStream {

		private final byte[] singleByteBuffer = new byte[1];
		private final boolean raw;

		public FileDataInputStream(boolean raw) {
			this.raw = raw;
		}

		@Override
		public int read() throws IOException {
			int val = read(singleByteBuffer, 0, 1);
			if (val < 0) {
				return -1;
			} else {
				return singleByteBuffer[0];
			}
		}

		@Override
		public int read(byte[] buffer, int offset, int lengeth) throws IOException {
			if (raw) {
				return readRawFileDataPart(buffer, offset, lengeth);
			} else {
				return readFileDataPart(buffer, offset, lengeth);
			}
		}

		@Override
		public void close() {
			// no-op, nothing to close
		}
	}
}
