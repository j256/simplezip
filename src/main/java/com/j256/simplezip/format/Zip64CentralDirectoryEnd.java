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
public class Zip64CentralDirectoryEnd {

	/** signature that is expected to be at the start of the central directory */
	private static final int EXPECTED_SIGNATURE = 0x6064b50;
	public static final int FIXED_FIELDS_SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

	private final int versionMade;
	private final int versionNeeded;
	private final int diskNumber;
	private final int diskNumberStart;
	private final long numRecordsOnDisk;
	private final long numRecordsTotal;
	private final long directorySize;
	private final long directoryOffset;
	private final byte[] extensibleData;

	public Zip64CentralDirectoryEnd(int versionMade, int versionNeeded, int diskNumber, int diskNumberStart,
			long numRecordsOnDisk, long numRecordsTotal, long directorySize, long directoryOffset,
			byte[] extensibleData) {
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.numRecordsOnDisk = numRecordsOnDisk;
		this.numRecordsTotal = numRecordsTotal;
		this.directorySize = directorySize;
		this.directoryOffset = directoryOffset;
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
	public static Zip64CentralDirectoryEnd read(RewindableInputStream inputStream) throws IOException {

		byte[] tmpBytes = new byte[8]; 
		int signature = IoUtils.readInt(inputStream, tmpBytes, "Zip64CentralDirectoryEnd.signature");
		if (signature != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}

		Builder builder = new Zip64CentralDirectoryEnd.Builder();
		long size = IoUtils.readLong(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.dirEndSize");
		builder.versionMade = IoUtils.readShort(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.versionMade");
		builder.versionNeeded = IoUtils.readShort(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.versionNeeded");
		builder.diskNumber = IoUtils.readInt(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.diskNumber");
		builder.diskNumberStart = IoUtils.readInt(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.diskNumberStart");
		builder.numRecordsOnDisk = IoUtils.readLong(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.numRecordsOnDisk");
		builder.numRecordsTotal = IoUtils.readLong(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.numRecordsTotal");
		builder.directorySize = IoUtils.readLong(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.sizeDirectory");
		builder.directoryOffset = IoUtils.readLong(inputStream, tmpBytes, "ZipCentralDirectoryFileEnd.directoryOffset");
		long extensibleDataLength = size - FIXED_FIELDS_SIZE;
		if (extensibleDataLength > Integer.MAX_VALUE) {
			// may never get here but let's be careful out there
			throw new IllegalArgumentException("Zip64 extensibleData length " + extensibleDataLength + "is too large");
		}
		builder.extensibleData =
				IoUtils.readBytes(inputStream, (int) extensibleDataLength, "ZipCentralDirectoryFileEnd.comment");
		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		byte[] tmpBytes = new byte[8]; 
		IoUtils.writeInt(outputStream, tmpBytes, EXPECTED_SIGNATURE);
		long size = FIXED_FIELDS_SIZE;
		if (extensibleData != null) {
			size += extensibleData.length;
		}
		IoUtils.writeLong(outputStream, tmpBytes, size);
		IoUtils.writeShort(outputStream, tmpBytes, versionMade);
		IoUtils.writeShort(outputStream, tmpBytes, versionNeeded);
		IoUtils.writeInt(outputStream, tmpBytes, diskNumber);
		IoUtils.writeInt(outputStream, tmpBytes, diskNumberStart);
		IoUtils.writeLong(outputStream, tmpBytes, numRecordsOnDisk);
		IoUtils.writeLong(outputStream, tmpBytes, numRecordsTotal);
		IoUtils.writeLong(outputStream, tmpBytes, directorySize);
		IoUtils.writeLong(outputStream, tmpBytes, (int) directoryOffset);
		IoUtils.writeBytes(outputStream, extensibleData);
	}

	public int getVersionMade() {
		return versionMade;
	}

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

	public byte[] getExtensibleData() {
		return extensibleData;
	}

	/**
	 * Builder for the {@link Zip64CentralDirectoryEnd}.
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
		private byte[] extensibleData;

		/**
		 * Build a copy of our directory end.
		 */
		public Zip64CentralDirectoryEnd build() {
			return new Zip64CentralDirectoryEnd(versionMade, versionNeeded, diskNumber, diskNumberStart,
					numRecordsOnDisk, numRecordsTotal, directorySize, directoryOffset, extensibleData);
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

		public byte[] getExtensibleData() {
			return extensibleData;
		}

		public void setExtensibleData(byte[] extensibleData) {
			this.extensibleData = extensibleData;
		}
	}
}
