package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * End segment of the central-directory which is at the very end of the Zip file.
 * 
 * @author graywatson
 */
public class CentralDirectoryEnd {

	/** signature that is expected to be at the start of the central directory */
	public static final int EXPECTED_SIGNATURE = 0x6054b50;

	private final int signature;
	private final int diskNumber;
	private final int diskNumberStart;
	private final int numRecordsOnDisk;
	private final int numRecordsTotal;
	private final int sizeDirectory;
	private final int offsetDirectory;
	private final byte[] commentBytes;

	public CentralDirectoryEnd(int signature, int diskNumber, int diskNumberStart, int numRecordsOnDisk,
			int numRecordsTotal, int sizeDirectory, int offsetDirectory, byte[] commentBytes) {
		this.signature = signature;
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.numRecordsOnDisk = numRecordsOnDisk;
		this.numRecordsTotal = numRecordsTotal;
		this.sizeDirectory = sizeDirectory;
		this.offsetDirectory = offsetDirectory;
		this.commentBytes = commentBytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read one from the input-stream.
	 */
	public static CentralDirectoryEnd read(RewindableInputStream inputStream) throws IOException {

		Builder builder = new CentralDirectoryEnd.Builder();

		builder.signature = IoUtils.readInt(inputStream, "CentralDirectoryEnd.signature");
		builder.diskNumber = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.diskNumber");
		builder.diskNumberStart = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.diskNumberStart");
		builder.numRecordsOnDisk = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.numRecordsOnDisk");
		builder.numRecordsTotal = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.numRecordsTotal");
		builder.sizeDirectory = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.sizeDirectory");
		builder.offsetDirectory = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.offsetDirectory");
		int commentLength = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.commentLength");
		builder.commentBytes = IoUtils.readBytes(inputStream, commentLength, "CentralDirectoryFileHeader.comment");

		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeShort(outputStream, diskNumber);
		IoUtils.writeShort(outputStream, diskNumberStart);
		IoUtils.writeShort(outputStream, numRecordsOnDisk);
		IoUtils.writeShort(outputStream, numRecordsTotal);
		IoUtils.writeInt(outputStream, sizeDirectory);
		IoUtils.writeInt(outputStream, offsetDirectory);
		if (commentBytes == null) {
			IoUtils.writeShort(outputStream, 0);
		} else {
			IoUtils.writeShort(outputStream, commentBytes.length);
		}
		if (commentBytes != null) {
			IoUtils.writeBytes(outputStream, commentBytes);
		}
	}

	public int getSignature() {
		return signature;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public int getNumRecordsOnDisk() {
		return numRecordsOnDisk;
	}

	public int getNumRecordsTotal() {
		return numRecordsTotal;
	}

	public int getSizeDirectory() {
		return sizeDirectory;
	}

	public int getOffsetDirectory() {
		return offsetDirectory;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	/**
	 * Builder for the {@link CentralDirectoryEnd}.
	 */
	public static class Builder {
		private int signature;
		private int diskNumber;
		private int diskNumberStart;
		private int numRecordsOnDisk;
		private int numRecordsTotal;
		private int sizeDirectory;
		private int offsetDirectory;
		private byte[] commentBytes;

		public int getSignature() {
			return signature;
		}

		public void setSignature(int signature) {
			this.signature = signature;
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}

		public int getDiskNumberStart() {
			return diskNumberStart;
		}

		public void setDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
		}

		public int getNumRecordsOnDisk() {
			return numRecordsOnDisk;
		}

		public void setNumRecordsOnDisk(int numRecordsOnDisk) {
			this.numRecordsOnDisk = numRecordsOnDisk;
		}

		public int getNumRecordsTotal() {
			return numRecordsTotal;
		}

		public void setNumRecordsTotal(int numRecordsTotal) {
			this.numRecordsTotal = numRecordsTotal;
		}

		public int getSizeDirectory() {
			return sizeDirectory;
		}

		public void setSizeDirectory(int sizeDirectory) {
			this.sizeDirectory = sizeDirectory;
		}

		public int getOffsetDirectory() {
			return offsetDirectory;
		}

		public void setOffsetDirectory(int offsetDirectory) {
			this.offsetDirectory = offsetDirectory;
		}

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}

		public CentralDirectoryEnd build() {
			return new CentralDirectoryEnd(signature, diskNumber, diskNumberStart, numRecordsOnDisk, numRecordsTotal,
					sizeDirectory, offsetDirectory, commentBytes);
		}
	}
}
