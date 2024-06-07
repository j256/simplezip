package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * End segment of the central-directory which is at the very end of the Zip file which can be either in Zip64 format or
 * older format.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryEnd {

	/** signature that is expected to be at the start of the central directory */
	private static final int EXPECTED_ZIP32_SIGNATURE = 0x6054b50;
	private static final int EXPECTED_ZIP64_SIGNATURE = 0x6064b50;
	/** This is the minimum size that this header will take on disk. */
	public static final int MINIMUM_ZIP32_READ_SIZE = 4 * 2 + 2 * 4 + 2;
	public static final int ZIP64_FIXED_FIELDS_SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

	private final boolean zip64;
	private final int versionMade;
	private final int versionNeeded;
	private final int diskNumber;
	private final int diskNumberStart;
	private final long numRecordsOnDisk;
	private final long numRecordsTotal;
	private final long directorySize;
	private final long directoryOffset;
	private final byte[] commentBytes;
	private final byte[] extensibleData;

	public ZipCentralDirectoryEnd(boolean zip64, int versionMade, int versionNeeded, int diskNumber,
			int diskNumberStart, long numRecordsOnDisk, long numRecordsTotal, long directorySize, long directoryOffset,
			byte[] commentBytes, byte[] extensibleData) {
		this.zip64 = zip64;
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.numRecordsOnDisk = numRecordsOnDisk;
		this.numRecordsTotal = numRecordsTotal;
		this.directorySize = directorySize;
		this.directoryOffset = directoryOffset;
		this.commentBytes = commentBytes;
		this.extensibleData = extensibleData;
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
	public static ZipCentralDirectoryEnd read(RewindableInputStream inputStream) throws IOException {

		int signature = IoUtils.readInt(inputStream, "CentralDirectoryEnd.signature");
		if (signature == EXPECTED_ZIP32_SIGNATURE) {
			Builder builder = new ZipCentralDirectoryEnd.Builder();
			builder.diskNumber = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.diskNumber");
			builder.diskNumberStart = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.diskNumberStart");
			builder.numRecordsOnDisk = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.numRecordsOnDisk");
			builder.numRecordsTotal = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.numRecordsTotal");
			builder.directorySize = IoUtils.readInt(inputStream, "ZipCentralDirectoryFileEnd.sizeDirectory");
			builder.directoryOffset = IoUtils.readInt(inputStream, "ZipCentralDirectoryFileEnd.directoryOffset");
			int commentLength = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.commentLength");
			builder.commentBytes = IoUtils.readBytes(inputStream, commentLength, "ZipCentralDirectoryFileEnd.comment");
			return builder.build();
		} else if (signature == EXPECTED_ZIP64_SIGNATURE) {
			Builder builder = new ZipCentralDirectoryEnd.Builder();
			long size = IoUtils.readLong(inputStream, "ZipCentralDirectoryFileEnd.dirEndSize");
			builder.versionMade = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.versionMade");
			builder.versionNeeded = IoUtils.readShort(inputStream, "ZipCentralDirectoryFileEnd.versionNeeded");
			builder.diskNumber = IoUtils.readInt(inputStream, "ZipCentralDirectoryFileEnd.diskNumber");
			builder.diskNumberStart = IoUtils.readInt(inputStream, "ZipCentralDirectoryFileEnd.diskNumberStart");
			builder.numRecordsOnDisk = IoUtils.readLong(inputStream, "ZipCentralDirectoryFileEnd.numRecordsOnDisk");
			builder.numRecordsTotal = IoUtils.readLong(inputStream, "ZipCentralDirectoryFileEnd.numRecordsTotal");
			builder.directorySize = IoUtils.readLong(inputStream, "ZipCentralDirectoryFileEnd.sizeDirectory");
			builder.directoryOffset = IoUtils.readLong(inputStream, "ZipCentralDirectoryFileEnd.directoryOffset");
			long extensibleDataLength = size - ZIP64_FIXED_FIELDS_SIZE;
			if (extensibleDataLength > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"Zip64 extensibleData length " + extensibleDataLength + "is too large");
			}
			builder.extensibleData =
					IoUtils.readBytes(inputStream, (int) extensibleDataLength, "ZipCentralDirectoryFileEnd.comment");
			return builder.build();
		} else {
			inputStream.rewind(4);
			return null;
		}
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		if (zip64) {
			IoUtils.writeInt(outputStream, EXPECTED_ZIP64_SIGNATURE);
			long size = ZIP64_FIXED_FIELDS_SIZE;
			if (extensibleData != null) {
				size += extensibleData.length;
			}
			IoUtils.writeLong(outputStream, size);
			IoUtils.writeShort(outputStream, versionMade);
			IoUtils.writeShort(outputStream, versionNeeded);
			IoUtils.writeInt(outputStream, diskNumber);
			IoUtils.writeInt(outputStream, diskNumberStart);
			IoUtils.writeLong(outputStream, numRecordsOnDisk);
			IoUtils.writeLong(outputStream, numRecordsTotal);
			IoUtils.writeLong(outputStream, directorySize);
			IoUtils.writeLong(outputStream, (int) directoryOffset);
			IoUtils.writeBytes(outputStream, extensibleData);
		} else {
			IoUtils.writeInt(outputStream, EXPECTED_ZIP32_SIGNATURE);
			IoUtils.writeShort(outputStream, diskNumber);
			IoUtils.writeShort(outputStream, diskNumberStart);
			IoUtils.writeShort(outputStream, (int) numRecordsOnDisk);
			IoUtils.writeShort(outputStream, (int) numRecordsTotal);
			IoUtils.writeInt(outputStream, directorySize);
			IoUtils.writeInt(outputStream, (int) directoryOffset);
			if (commentBytes == null) {
				IoUtils.writeShort(outputStream, 0);
				// no comment-bytes
			} else {
				IoUtils.writeShort(outputStream, commentBytes.length);
				IoUtils.writeBytes(outputStream, commentBytes);
			}
		}
	}

	/**
	 * Return whether or not this is a Zip64 or not. This indicates whether or not certain of the fields are enabled.
	 */
	public boolean isZip64() {
		return zip64;
	}

	/**
	 * If a zip64 end then this value will be provided.
	 */
	public int getVersionMade() {
		return versionMade;
	}

	/**
	 * If a zip64 end then this value will be provided.
	 */
	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public long getNumRecordsOnDisk() {
		return numRecordsOnDisk;
	}

	public long getNumRecordsTotal() {
		return numRecordsTotal;
	}

	public long getDirectorySize() {
		return directorySize;
	}

	public long getDirectoryOffset() {
		return directoryOffset;
	}

	/**
	 * Comment data if it is _not_ a zip64 end.
	 */
	public byte[] getCommentBytes() {
		return commentBytes;
	}

	/**
	 * Comment data if it is _not_ a zip64 end.
	 */
	public String getComment() {
		if (commentBytes == null) {
			return null;
		} else {
			return new String(commentBytes);
		}
	}

	/**
	 * Extensible extra data if a zip64 end.
	 */
	public byte[] getExtensibleData() {
		return extensibleData;
	}

	/**
	 * Builder for the {@link ZipCentralDirectoryEnd}.
	 */
	public static class Builder {
		private int versionMade;
		private int versionNeeded;
		private int diskNumber = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private long numRecordsOnDisk;
		private long numRecordsTotal;
		private long directorySize;
		private long directoryOffset;
		private byte[] commentBytes;
		private byte[] extensibleData;

		/**
		 * Build a copy of our directory end.
		 */
		public ZipCentralDirectoryEnd build() {
			boolean zip64 = false;
			if (extensibleData != null) {
				if (commentBytes != null) {
					throw new IllegalArgumentException(
							"Cannot set both the comment (old end) and extensibleData (zip64 end)");
				}
				zip64 = true;
			}
			if (versionMade != 0 //
					|| versionNeeded != 0 //
					|| diskNumber > IoUtils.MAX_UNSIGNED_SHORT_VALUE
					|| diskNumberStart > IoUtils.MAX_UNSIGNED_SHORT_VALUE
					|| numRecordsOnDisk > IoUtils.MAX_UNSIGNED_SHORT_VALUE
					|| numRecordsTotal > IoUtils.MAX_UNSIGNED_SHORT_VALUE
					|| directorySize > IoUtils.MAX_UNSIGNED_INT_VALUE
					|| directoryOffset > IoUtils.MAX_UNSIGNED_INT_VALUE) {
				zip64 = true;
			}
			return new ZipCentralDirectoryEnd(zip64, versionMade, versionNeeded, diskNumber, diskNumberStart,
					numRecordsOnDisk, numRecordsTotal, directorySize, directoryOffset, commentBytes, extensibleData);
		}

		/**
		 * Create an end from the directory-end-info.
		 */
		public static Builder fromEndInfo(ZipCentralDirectoryEndInfo endInfo) {
			Builder builder = new Builder();
			builder.versionMade = endInfo.getVersionMade();
			builder.versionNeeded = endInfo.getVersionNeeded();
			builder.diskNumber = endInfo.getDiskNumber();
			builder.diskNumberStart = endInfo.getDiskNumberStart();
			builder.commentBytes = endInfo.getCommentBytes();
			builder.extensibleData = endInfo.getExtensibleData();
			return builder;
		}

		/**
		 * If a zip64 end then this field is available.
		 */
		public int getVersionMade() {
			return versionMade;
		}

		/**
		 * If a zip64 end then this field is available.
		 */
		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
		}

		/**
		 * If a zip64 end then this field is available.
		 */
		public int getVersionNeeded() {
			return versionNeeded;
		}

		/**
		 * If a zip64 end then this field is available.
		 */
		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
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

		public long getNumRecordsOnDisk() {
			return numRecordsOnDisk;
		}

		public void setNumRecordsOnDisk(long numRecordsOnDisk) {
			this.numRecordsOnDisk = numRecordsOnDisk;
		}

		public long getNumRecordsTotal() {
			return numRecordsTotal;
		}

		public void setNumRecordsTotal(long numRecordsTotal) {
			this.numRecordsTotal = numRecordsTotal;
		}

		public long getDirectorySize() {
			return directorySize;
		}

		public void setDirectorySize(long directorySize) {
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

		public byte[] getExtensibleData() {
			return extensibleData;
		}

		public void setExtensibleData(byte[] extensibleData) {
			this.extensibleData = extensibleData;
		}
	}
}
