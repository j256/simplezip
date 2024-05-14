package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Set;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.ZipStatus;

/**
 * Header of Zip file entries.
 * 
 * @author graywatson
 */
public class ZipFileHeader {

	private static int EXPECTED_SIGNATURE = 0x4034b50;

	private final int versionNeeded;
	private final Set<GeneralPurposeFlag> generalPurposeFlags;
	private final int generalPurposeFlagsValue;
	private final int compressionMethodValue;
	private final int lastModifiedFileTime;
	private final int lastModifiedFileDate;
	private final long crc32;
	private final int compressedSize;
	private final int uncompressedSize;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;

	public ZipFileHeader(int versionNeeded, int generalPurposeFlagsValue, int compressionMethod,
			int lastModifiedFileTime, int lastModifiedFileDate, long crc32, int compressedSize, int uncompressedSize,
			byte[] fileName, byte[] extraFieldBytes) {
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = GeneralPurposeFlag.fromInt(generalPurposeFlagsValue);
		this.generalPurposeFlagsValue = generalPurposeFlagsValue;
		this.compressionMethodValue = compressionMethod;
		this.lastModifiedFileTime = lastModifiedFileTime;
		this.lastModifiedFileDate = lastModifiedFileDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.fileNameBytes = fileName;
		this.extraFieldBytes = extraFieldBytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read from the input stream.
	 */
	public static ZipFileHeader read(RewindableInputStream inputStream) throws IOException {
		Builder builder = new ZipFileHeader.Builder();
		/*
		 * WHen reading a file-header we aren't sure if this is a file-header or the start of the central directory.
		 */
		int first = IoUtils.readInt(inputStream, "LocalFileHeader.signature");
		if (first == CentralDirectoryFileHeader.EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}
		builder.versionNeeded = IoUtils.readShort(inputStream, "LocalFileHeader.versionNeeded");
		builder.generalPurposeFlagsValue = IoUtils.readShort(inputStream, "LocalFileHeader.generalPurposeFlags");
		builder.compressionMethodValue = IoUtils.readShort(inputStream, "LocalFileHeader.compressionMethod");
		builder.lastModifiedFileTime = IoUtils.readShort(inputStream, "LocalFileHeader.lastModFileTime");
		builder.lastModifiedFileDate = IoUtils.readShort(inputStream, "LocalFileHeader.lastModFileDate");
		builder.crc32 = IoUtils.readInt(inputStream, "LocalFileHeader.crc32");
		builder.compressedSize = IoUtils.readInt(inputStream, "LocalFileHeader.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(inputStream, "LocalFileHeader.uncompressedSize");
		int fileNameLength = IoUtils.readShort(inputStream, "LocalFileHeader.fileNameLength");
		int extraLength = IoUtils.readShort(inputStream, "LocalFileHeader.extraLength");
		builder.fileNameBytes = IoUtils.readBytes(inputStream, fileNameLength, "LocalFileHeader.fileName");
		builder.extraFieldBytes = IoUtils.readBytes(inputStream, extraLength, "LocalFileHeader.extra");
		return builder.build();
	}

	/**
	 * Write to the input stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeShort(outputStream, versionNeeded);
		IoUtils.writeShort(outputStream, generalPurposeFlagsValue);
		IoUtils.writeShort(outputStream, compressionMethodValue);
		IoUtils.writeShort(outputStream, lastModifiedFileTime);
		IoUtils.writeShort(outputStream, lastModifiedFileDate);
		IoUtils.writeInt(outputStream, crc32);
		IoUtils.writeInt(outputStream, compressedSize);
		IoUtils.writeInt(outputStream, uncompressedSize);
		IoUtils.writeShortBytesLength(outputStream, fileNameBytes);
		IoUtils.writeShortBytesLength(outputStream, extraFieldBytes);
		IoUtils.writeBytes(outputStream, fileNameBytes);
		IoUtils.writeBytes(outputStream, extraFieldBytes);
	}

	/**
	 * Validate the returned header.
	 */
	public ZipStatus validate() {
		return ZipStatus.OK;
	}

	/**
	 * Return whether the header has this flag.
	 */
	public boolean hasFlag(GeneralPurposeFlag flag) {
		return generalPurposeFlags.contains(flag);
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getGeneralPurposeFlagsValue() {
		return generalPurposeFlagsValue;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public CompressionMethod getCompressionMethod() {
		return CompressionMethod.fromValue(compressionMethodValue);
	}

	public int getCompressionMethodValue() {
		return compressionMethodValue;
	}

	public int getLastModifiedFileTime() {
		return lastModifiedFileTime;
	}

	/**
	 * Return last modified time as a string in 24-hour HH:MM:SS format.
	 */
	public String getLastModifiedFileTimeString() {
		String result = String.format("%d:%02d:%02d", (lastModifiedFileTime >> 11),
				((lastModifiedFileTime >> 5) & 0x3F), ((lastModifiedFileTime & 0x1F) * 2));
		return result;
	}

	public int getLastModifiedFileDate() {
		return lastModifiedFileDate;
	}

	/**
	 * Return last modified date as a string in YYYY.mm.dd format.
	 */
	public String getLastModifiedFileDateString() {
		String result = String.format("%d.%02d.%02d", (((lastModifiedFileDate >> 9) & 0x7F) + 1980),
				((lastModifiedFileDate >> 5) & 0x0F), (lastModifiedFileDate & 0x1F));
		return result;
	}

	/**
	 * Return last modified date and time as a {@link LocalDateTime}.
	 */
	public LocalDateTime getLastModFileDateTime() {
		LocalDateTime localDateTime = LocalDateTime.of((((lastModifiedFileDate >> 9) & 0x7F) + 1980),
				((lastModifiedFileDate >> 5) & 0x0F), (lastModifiedFileDate & 0x1F), (lastModifiedFileTime >> 11),
				((lastModifiedFileTime >> 5) & 0x3F), ((lastModifiedFileTime & 0x1F) * 2));
		return localDateTime;
	}

	public long getCrc32() {
		return crc32;
	}

	public int getCompressedSize() {
		return compressedSize;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public byte[] getFileNameBytes() {
		return fileNameBytes;
	}

	public String getFileName() {
		return new String(fileNameBytes);
	}

	public byte[] getExtraFieldBytes() {
		return extraFieldBytes;
	}

	@Override
	public String toString() {
		return "ZipFileHeader [name=" + getFileName() + ", compSize " + compressedSize + ", uncompSize="
				+ uncompressedSize + ", extra-#-bytes=" + (extraFieldBytes == null ? "null" : extraFieldBytes.length)
				+ "]";
	}

	/**
	 * Builder for the LocalFileHeader class.
	 */
	public static class Builder {
		private int versionNeeded;
		/** NOTE: we turn on the data-descriptor flag by default until a size or crc32 is set */
		private int generalPurposeFlagsValue = GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		private int compressionMethodValue;
		private int lastModifiedFileTime;
		private int lastModifiedFileDate;
		private long crc32;
		private int compressedSize;
		private int uncompressedSize;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;

		public ZipFileHeader build() {
			return new ZipFileHeader(versionNeeded, generalPurposeFlagsValue, compressionMethodValue,
					lastModifiedFileTime, lastModifiedFileDate, crc32, compressedSize, uncompressedSize, fileNameBytes,
					extraFieldBytes);
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public int getGeneralPurposeFlagsValue() {
			return generalPurposeFlagsValue;
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(Set)}.
		 */
		public void setGeneralPurposeFlagsValue(int generalPurposeFlagsValue) {
			this.generalPurposeFlagsValue = generalPurposeFlagsValue;
		}

		/**
		 * Adds to the current general-purpose-flags the value associated with this flag enum.
		 */
		public void addGeneralPurposeFlag(GeneralPurposeFlag flag) {
			this.generalPurposeFlagsValue |= flag.getValue();
		}

		public Set<GeneralPurposeFlag> getGeneralPurposeFlags() {
			return GeneralPurposeFlag.fromInt(generalPurposeFlagsValue);
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlagsValue(int)}.
		 */
		public void setGeneralPurposeFlags(Set<GeneralPurposeFlag> generalPurposeFlags) {
			for (GeneralPurposeFlag flag : generalPurposeFlags) {
				generalPurposeFlagsValue |= flag.getValue();
			}
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlagsValue(int)}.
		 */
		public void setGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlags) {
			for (GeneralPurposeFlag flag : generalPurposeFlags) {
				generalPurposeFlagsValue |= flag.getValue();
			}
		}

		public CompressionMethod getCompressionMethod() {
			return CompressionMethod.fromValue(compressionMethodValue);
		}

		public void setCompressionMethod(CompressionMethod method) {
			this.compressionMethodValue = method.getValue();
		}

		public int getCompressionMethodValue() {
			return compressionMethodValue;
		}

		public void setCompressionMethodValue(int compressionMethod) {
			this.compressionMethodValue = compressionMethod;
		}

		public int getLastModifiedFileTime() {
			return lastModifiedFileTime;
		}

		public void setLastModifiedFileTime(int lastModFileTime) {
			this.lastModifiedFileTime = lastModFileTime;
		}

		public int getLastModifiedFileDate() {
			return lastModifiedFileDate;
		}

		public void setLastModifiedFileDate(int lastModifiedFileDate) {
			this.lastModifiedFileDate = lastModifiedFileDate;
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as a {@link LocalDateTime}. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public void setLastModifiedDateTime(LocalDateTime localDateTime) {
			this.lastModifiedFileDate = (((localDateTime.getYear() - 1980) << 9) | (localDateTime.getMonthValue() << 5)
					| (localDateTime.getDayOfMonth()));
			this.lastModifiedFileTime = ((localDateTime.getHour() << 11) | (localDateTime.getMinute() << 5)
					| (localDateTime.getSecond() / 2));
		}

		public long getCrc32() {
			return crc32;
		}

		public void setCrc32(long crc32) {
			if (crc32 != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlagsValue &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
			}
			this.crc32 = crc32;
		}

		public int getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(int compressedSize) {
			if (compressedSize != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlagsValue &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
			}
			this.compressedSize = compressedSize;
		}

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(int uncompressedSize) {
			if (uncompressedSize != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlagsValue &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
			}
			this.uncompressedSize = uncompressedSize;
		}

		public byte[] getFileNameBytes() {
			return fileNameBytes;
		}

		public void setFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
		}

		public void setFileName(String fileName) {
			this.fileNameBytes = fileName.getBytes();
		}

		public byte[] getExtraFieldBytes() {
			return extraFieldBytes;
		}

		public void setExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
		}
	}
}
