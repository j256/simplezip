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

	private final int diskNumber;
	private final int diskNumberStart;
	private final int numRecordsOnDisk;
	private final int numRecordsTotal;
	private final int directorySize;
	private final long directoryOffset;
	private final byte[] commentBytes;

	public CentralDirectoryEnd(int diskNumber, int diskNumberStart, int numRecordsOnDisk, int numRecordsTotal,
			int directorySize, long directoryOffset, byte[] commentBytes) {
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.numRecordsOnDisk = numRecordsOnDisk;
		this.numRecordsTotal = numRecordsTotal;
		this.directorySize = directorySize;
		this.directoryOffset = directoryOffset;
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

		IoUtils.readInt(inputStream, "CentralDirectoryEnd.signature");
		// XXX: need to throw if not directory-end or valadation issue
		builder.diskNumber = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.diskNumber");
		builder.diskNumberStart = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.diskNumberStart");
		builder.numRecordsOnDisk = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.numRecordsOnDisk");
		builder.numRecordsTotal = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.numRecordsTotal");
		builder.directorySize = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.sizeDirectory");
		builder.directoryOffset = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.directoryOffset");
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
		IoUtils.writeInt(outputStream, directorySize);
		// XXX: need to handle zip64
		IoUtils.writeInt(outputStream, (int) directoryOffset);
		if (commentBytes == null) {
			IoUtils.writeShort(outputStream, 0);
		} else {
			IoUtils.writeShort(outputStream, commentBytes.length);
		}
		if (commentBytes != null) {
			IoUtils.writeBytes(outputStream, commentBytes);
		}
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

	public int getDirectorySize() {
		return directorySize;
	}

	public long getDirectoryOffset() {
		return directoryOffset;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	/**
	 * Builder for the {@link CentralDirectoryEnd}.
	 */
	public static class Builder {
		private int diskNumber = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
		private int numRecordsOnDisk;
		private int numRecordsTotal;
		private int directorySize;
		private long directoryOffset;
		private byte[] commentBytes;

		/**
		 * Create a builder from an existing directory-end
		 */
		public static Builder fromEnd(CentralDirectoryEnd end) {
			Builder builder = new Builder();
			builder.diskNumber = end.diskNumber;
			builder.diskNumberStart = end.diskNumberStart;
			builder.numRecordsOnDisk = end.numRecordsOnDisk;
			builder.numRecordsTotal = end.numRecordsTotal;
			builder.directorySize = end.directorySize;
			builder.directoryOffset = end.directoryOffset;
			builder.commentBytes = end.commentBytes;
			return builder;
		}

		/**
		 * Reset the builder in case you want to reuse.
		 */
		public void reset() {
			diskNumber = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
			diskNumberStart = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
			numRecordsOnDisk = 0;
			numRecordsTotal = 0;
			directorySize = 0;
			directoryOffset = 0;
			commentBytes = null;
		}

		public CentralDirectoryEnd build() {
			return new CentralDirectoryEnd(diskNumber, diskNumberStart, numRecordsOnDisk, numRecordsTotal,
					directorySize, directoryOffset, commentBytes);
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

		public int getDirectorySize() {
			return directorySize;
		}

		public void setDirectorySize(int directorySize) {
			this.directorySize = directorySize;
		}

		public long getDirectoryOffset() {
			return directoryOffset;
		}

		public void setDirectoryOffset(long directoryOffset) {
			this.directoryOffset = directoryOffset;
		}

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}
	}
}
