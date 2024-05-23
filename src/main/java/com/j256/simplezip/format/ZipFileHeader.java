package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Header of Zip file entries.
 * 
 * @author graywatson
 */
public class ZipFileHeader {

	private static int EXPECTED_SIGNATURE = 0x4034b50;

	private final int versionNeeded;
	private final int generalPurposeFlags;
	private final int compressionMethod;
	private final int lastModifiedTime;
	private final int lastModifiedDate;
	private final long crc32;
	private final int compressedSize;
	private final int uncompressedSize;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;

	public ZipFileHeader(int versionNeeded, int generalPurposeFlags, int compressionMethod, int lastModifiedTime,
			int lastModifiedDate, long crc32, int compressedSize, int uncompressedSize, byte[] fileName,
			byte[] extraFieldBytes) {
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = generalPurposeFlags;
		this.compressionMethod = compressionMethod;
		this.lastModifiedTime = lastModifiedTime;
		this.lastModifiedDate = lastModifiedDate;
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
		if (first != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}
		builder.versionNeeded = IoUtils.readShort(inputStream, "LocalFileHeader.versionNeeded");
		builder.generalPurposeFlags = IoUtils.readShort(inputStream, "LocalFileHeader.generalPurposeFlags");
		builder.compressionMethod = IoUtils.readShort(inputStream, "LocalFileHeader.compressionMethod");
		builder.lastModifiedTime = IoUtils.readShort(inputStream, "LocalFileHeader.lastModifiedTime");
		builder.lastModifiedDate = IoUtils.readShort(inputStream, "LocalFileHeader.lastModifiedDate");
		builder.crc32 = IoUtils.readIntAsLong(inputStream, "LocalFileHeader.crc32");
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
		int flags = generalPurposeFlags;
		if (needsDataDescriptor()) {
			flags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		}
		IoUtils.writeShort(outputStream, flags);
		IoUtils.writeShort(outputStream, compressionMethod);
		IoUtils.writeShort(outputStream, lastModifiedTime);
		IoUtils.writeShort(outputStream, lastModifiedDate);
		IoUtils.writeInt(outputStream, crc32);
		IoUtils.writeInt(outputStream, compressedSize);
		IoUtils.writeInt(outputStream, uncompressedSize);
		IoUtils.writeShortBytesLength(outputStream, fileNameBytes);
		IoUtils.writeShortBytesLength(outputStream, extraFieldBytes);
		IoUtils.writeBytes(outputStream, fileNameBytes);
		IoUtils.writeBytes(outputStream, extraFieldBytes);
	}

	/**
	 * Return whether the header has this flag.
	 */
	public boolean hasFlag(GeneralPurposeFlag flag) {
		return ((generalPurposeFlags & flag.getValue()) == flag.getValue());
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	/**
	 * Get the version needed as a processed enum.
	 */
	public ZipVersion getZipVersionNeeded() {
		return ZipVersion.fromValue(versionNeeded);
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlagsAsEnums() {
		return GeneralPurposeFlag.fromInt(generalPurposeFlags);
	}

	/**
	 * Read the compression level from the flags.
	 */
	public int getCompressionLevel() {
		int deflateFlags = (generalPurposeFlags & 06);
		if (deflateFlags == GeneralPurposeFlag.DEFLATING_MAXIMUM.getValue()) {
			return Deflater.BEST_COMPRESSION;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_NORMAL.getValue()) {
			return Deflater.DEFAULT_COMPRESSION;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_FAST.getValue()) {
			// i guess this is right
			return (Deflater.DEFAULT_COMPRESSION + Deflater.BEST_SPEED) / 2;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_SUPER_FAST.getValue()) {
			return Deflater.BEST_SPEED;
		} else {
			// may not get here but let's be careful out there
			return Deflater.DEFAULT_COMPRESSION;
		}
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}

	public CompressionMethod getCompressionMethodAsEnum() {
		return CompressionMethod.fromValue(compressionMethod);
	}

	public int getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * Return last modified time as a string in 24-hour HH:MM:SS format.
	 */
	public String getLastModifiedTimeString() {
		String result = String.format("%d:%02d:%02d", (lastModifiedTime >> 11), ((lastModifiedTime >> 5) & 0x3F),
				((lastModifiedTime & 0x1F) * 2));
		return result;
	}

	public int getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * Return last modified date as a string in YYYY.mm.dd format.
	 */
	public String getLastModifiedDateString() {
		String result = String.format("%d.%02d.%02d", (((lastModifiedDate >> 9) & 0x7F) + 1980),
				((lastModifiedDate >> 5) & 0x0F), (lastModifiedDate & 0x1F));
		return result;
	}

	/**
	 * Return last modified date and time as a {@link LocalDateTime}.
	 */
	public LocalDateTime getLastModifiedDateTime() {
		LocalDateTime localDateTime = LocalDateTime.of((((lastModifiedDate >> 9) & 0x7F) + 1980),
				((lastModifiedDate >> 5) & 0x0F), (lastModifiedDate & 0x1F), (lastModifiedTime >> 11),
				((lastModifiedTime >> 5) & 0x3F), ((lastModifiedTime & 0x1F) * 2));
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
		if (fileNameBytes == null) {
			return null;
		} else {
			return new String(fileNameBytes);
		}
	}

	public byte[] getExtraFieldBytes() {
		return extraFieldBytes;
	}

	/**
	 * Does this file header need a data-descriptor.
	 */
	public boolean needsDataDescriptor() {
		return (compressionMethod != CompressionMethod.NONE.getValue() && (compressedSize == 0 || crc32 == 0));
	}

	@Override
	public String toString() {
		return "ZipFileHeader [name=" + getFileName() + ", method=" + compressionMethod + ", compSize=" + compressedSize
				+ ", uncompSize=" + uncompressedSize + ", extra-#-bytes="
				+ (extraFieldBytes == null ? "null" : extraFieldBytes.length) + "]";
	}

	/**
	 * Builder for the LocalFileHeader class.
	 */
	public static class Builder {
		private int versionNeeded;
		/** NOTE: we turn on the data-descriptor flag by default until a size or crc32 is set */
		private int generalPurposeFlags;
		private int compressionMethod = CompressionMethod.DEFLATED.getValue();
		private int lastModifiedTime;
		private int lastModifiedDate;
		private long crc32;
		private int compressedSize;
		private int uncompressedSize;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;

		public Builder() {
			setLastModifiedDateTime(LocalDateTime.now());
		}

		public ZipFileHeader build() {
			return new ZipFileHeader(versionNeeded, generalPurposeFlags, compressionMethod, lastModifiedTime,
					lastModifiedDate, crc32, compressedSize, uncompressedSize, fileNameBytes, extraFieldBytes);
		}

		/**
		 * Start a builder from a previous Zip file-header.
		 */
		public static Builder fromHeader(ZipFileHeader header) {
			Builder builder = new Builder();
			builder.versionNeeded = header.versionNeeded;
			builder.generalPurposeFlags = header.generalPurposeFlags;
			builder.compressionMethod = header.compressionMethod;
			builder.lastModifiedTime = header.lastModifiedTime;
			builder.lastModifiedDate = header.lastModifiedDate;
			builder.crc32 = header.crc32;
			builder.compressedSize = header.compressedSize;
			builder.uncompressedSize = header.uncompressedSize;
			builder.fileNameBytes = header.fileNameBytes;
			builder.extraFieldBytes = header.extraFieldBytes;
			return builder;
		}

		/**
		 * Clear all fields in the builder. This does set a couple of default fields.
		 */
		public void reset() {
			versionNeeded = 0;
			generalPurposeFlags = 0;
			compressionMethod = CompressionMethod.DEFLATED.getValue();
			lastModifiedTime = 0;
			lastModifiedDate = 0;
			crc32 = 0;
			compressedSize = 0;
			uncompressedSize = 0;
			fileNameBytes = null;
			extraFieldBytes = null;
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public Builder withVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
			return this;
		}

		public int getGeneralPurposeFlags() {
			return generalPurposeFlags;
		}

		/**
		 * Sets the general-purpose-flags as a integer value. This overrides the value set by
		 * {@link #addGeneralPurposeFlags(Collection)}.
		 */
		public void setGeneralPurposeFlags(int generalPurposeFlags) {
			this.generalPurposeFlags = generalPurposeFlags;
		}

		/**
		 * Sets the general-purpose-flags as a integer value. This overrides the value set by
		 * {@link #addGeneralPurposeFlags(Collection)}.
		 */
		public Builder withGeneralPurposeFlags(int generalPurposeFlags) {
			this.generalPurposeFlags = generalPurposeFlags;
			return this;
		}

		/**
		 * Assign a flag via turning on and off.
		 */
		public void assignGeneralPurposeFlag(GeneralPurposeFlag flag, boolean value) {
			if (value) {
				this.generalPurposeFlags |= flag.getValue();
			} else {
				this.generalPurposeFlags &= ~flag.getValue();
			}
		}

		/**
		 * Return the set of GeneralPurposeFlag enums that make up the general-purpose-flags.
		 */
		public Set<GeneralPurposeFlag> getGeneralPurposeFlagAsEnums() {
			return GeneralPurposeFlag.fromInt(generalPurposeFlags);
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlags(Collection<GeneralPurposeFlag> generalPurposeFlagSet) {
			for (GeneralPurposeFlag flag : generalPurposeFlagSet) {
				generalPurposeFlags |= flag.getValue();
			}
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public Builder withGeneralPurposeFlags(Collection<GeneralPurposeFlag> generalPurposeFlagSet) {
			addGeneralPurposeFlags(generalPurposeFlagSet);
			return this;
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlagEnums) {
			for (GeneralPurposeFlag flag : generalPurposeFlagEnums) {
				if (flag == GeneralPurposeFlag.DEFLATING_NORMAL //
						|| flag == GeneralPurposeFlag.DEFLATING_MAXIMUM //
						|| flag == GeneralPurposeFlag.DEFLATING_FAST //
						|| flag == GeneralPurposeFlag.DEFLATING_SUPER_FAST) {
					generalPurposeFlags = (generalPurposeFlags & ~06) | flag.getValue();
				} else {
					generalPurposeFlags |= flag.getValue();
				}
			}
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public Builder withGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlagEnums) {
			addGeneralPurposeFlags(generalPurposeFlagEnums);
			return this;
		}

		public int getCompressionMethod() {
			return compressionMethod;
		}

		public void setCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
		}

		public Builder withCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
			return this;
		}

		public CompressionMethod getCompressionMethodAsEnum() {
			return CompressionMethod.fromValue(compressionMethod);
		}

		public void setCompressionMethod(CompressionMethod method) {
			this.compressionMethod = method.getValue();
		}

		public Builder withCompressionMethod(CompressionMethod method) {
			this.compressionMethod = method.getValue();
			return this;
		}

		public int getLastModifiedTime() {
			return lastModifiedTime;
		}

		public void setLastModifiedTime(int lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}

		public Builder withLastModifiedTime(int lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
			return this;
		}

		public int getLastModifiedDate() {
			return lastModifiedDate;
		}

		public void setLastModifiedDate(int lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
		}

		public Builder withLastModifiedDate(int lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
			return this;
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as a {@link LocalDateTime}. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public void setLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
			this.lastModifiedDate = (((lastModifiedDateTime.getYear() - 1980) << 9)
					| (lastModifiedDateTime.getMonthValue() << 5) | (lastModifiedDateTime.getDayOfMonth()));
			this.lastModifiedTime = ((lastModifiedDateTime.getHour() << 11) | (lastModifiedDateTime.getMinute() << 5)
					| (lastModifiedDateTime.getSecond() / 2));
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as a {@link LocalDateTime}. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public Builder withLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
			setLastModifiedDateTime(lastModifiedDateTime);
			return this;
		}

		public long getCrc32() {
			return crc32;
		}

		public void setCrc32(long crc32) {
			this.crc32 = crc32;
		}

		public Builder withCrc32(long crc32) {
			this.crc32 = crc32;
			return this;
		}

		public void setCrc32Value(CRC32 crc32) {
			this.crc32 = crc32.getValue();
		}

		public int getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(int compressedSize) {
			this.compressedSize = compressedSize;
		}

		public Builder withCompressedSize(int compressedSize) {
			this.compressedSize = compressedSize;
			return this;
		}

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(int uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}

		public Builder withUncompressedSize(int uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
			return this;
		}

		public byte[] getFileNameBytes() {
			return fileNameBytes;
		}

		public void setFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
		}

		public Builder withFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
			return this;
		}

		public String getFileName() {
			if (fileNameBytes == null) {
				return null;
			} else {
				return new String(fileNameBytes);
			}
		}

		public void setFileName(String fileName) {
			this.fileNameBytes = fileName.getBytes();
		}

		public Builder withFileName(String fileName) {
			this.fileNameBytes = fileName.getBytes();
			return this;
		}

		public byte[] getExtraFieldBytes() {
			return extraFieldBytes;
		}

		public void setExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
		}

		public Builder withExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
			return this;
		}
	}
}
