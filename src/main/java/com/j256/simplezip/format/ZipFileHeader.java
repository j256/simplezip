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
	private final int lastModifiedFileTime;
	private final int lastModifiedFileDate;
	private final long crc32;
	private final int compressedSize;
	private final int uncompressedSize;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;

	public ZipFileHeader(int versionNeeded, int generalPurposeFlags, int compressionMethod, int lastModifiedFileTime,
			int lastModifiedFileDate, long crc32, int compressedSize, int uncompressedSize, byte[] fileName,
			byte[] extraFieldBytes) {
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = generalPurposeFlags;
		this.compressionMethod = compressionMethod;
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
		if (first != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}
		builder.versionNeeded = IoUtils.readShort(inputStream, "LocalFileHeader.versionNeeded");
		builder.generalPurposeFlags = IoUtils.readShort(inputStream, "LocalFileHeader.generalPurposeFlags");
		builder.compressionMethod = IoUtils.readShort(inputStream, "LocalFileHeader.compressionMethod");
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
		int flags = generalPurposeFlags;
		if (needsDataDescriptor()) {
			flags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		}
		IoUtils.writeShort(outputStream, flags);
		IoUtils.writeShort(outputStream, compressionMethod);
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
	 * Return whether the header has this flag.
	 */
	public boolean hasFlag(GeneralPurposeFlag flag) {
		return ((generalPurposeFlags & flag.getValue()) != 0);
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlagAsEnums() {
		return GeneralPurposeFlag.fromInt(generalPurposeFlags);
	}

	/**
	 * Read the compression level from the flags.
	 */
	public int getCompressionLevel() {
		if ((compressionMethod & GeneralPurposeFlag.DEFLATING_MAXIMUM.getValue()) != 0) {
			return Deflater.BEST_COMPRESSION;
		} else if ((compressionMethod & GeneralPurposeFlag.DEFLATING_NORMAL.getValue()) != 0) {
			return Deflater.DEFAULT_COMPRESSION;
		} else if ((compressionMethod & GeneralPurposeFlag.DEFLATING_FAST.getValue()) != 0) {
			// i guess this is right
			return Deflater.DEFAULT_COMPRESSION + Deflater.BEST_SPEED / 2;
		} else if ((compressionMethod & GeneralPurposeFlag.DEFLATING_SUPER_FAST.getValue()) != 0) {
			return Deflater.BEST_SPEED;
		} else {
			return Deflater.DEFAULT_COMPRESSION;
		}
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}

	public CompressionMethod getCompressionMethodAsEnum() {
		return CompressionMethod.fromValue(compressionMethod);
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

	/**
	 * Does this file header need a data-descriptor.
	 */
	public boolean needsDataDescriptor() {
		return (compressedSize == 0 || crc32 == 0);
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
		private int generalPurposeFlags;
		private int compressionMethod = CompressionMethod.DEFLATED.getValue();
		private int lastModifiedFileTime;
		private int lastModifiedFileDate;
		private long crc32;
		private int compressedSize;
		private int uncompressedSize;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;

		public ZipFileHeader build() {
			return new ZipFileHeader(versionNeeded, generalPurposeFlags, compressionMethod, lastModifiedFileTime,
					lastModifiedFileDate, crc32, compressedSize, uncompressedSize, fileNameBytes, extraFieldBytes);
		}

		/**
		 * Start a builder from a previous Zip file-header.
		 */
		public static Builder fromHeader(ZipFileHeader header) {
			Builder builder = new Builder();
			builder.versionNeeded = header.versionNeeded;
			builder.generalPurposeFlags = header.generalPurposeFlags;
			builder.compressionMethod = header.compressionMethod;
			builder.lastModifiedFileTime = header.lastModifiedFileTime;
			builder.lastModifiedFileDate = header.lastModifiedFileDate;
			builder.crc32 = header.crc32;
			builder.compressedSize = header.compressedSize;
			builder.uncompressedSize = header.uncompressedSize;
			builder.fileNameBytes = header.fileNameBytes;
			builder.extraFieldBytes = header.extraFieldBytes;
			return builder;
		}

		/**
		 * Clear all fields in the builder.
		 */
		public void reset() {
			this.versionNeeded = 0;
			this.generalPurposeFlags = 0;
			this.compressionMethod = 0;
			this.lastModifiedFileTime = 0;
			this.lastModifiedFileDate = 0;
			this.crc32 = 0;
			this.compressedSize = 0;
			this.uncompressedSize = 0;
			this.fileNameBytes = null;
			this.extraFieldBytes = null;
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public int getGeneralPurposeFlags() {
			return generalPurposeFlags;
		}

		/**
		 * Sets the general-purpose-flags as a integer value. This overrides the value set by
		 * {@link #addGeneralPurposeFlags(Set)}.
		 */
		public void setGeneralPurposeFlags(int generalPurposeFlags) {
			this.generalPurposeFlags = generalPurposeFlags;
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
		 * {@link #setGeneralPurposeFlagsValue(int)}.
		 */
		public void addGeneralPurposeFlags(Collection<GeneralPurposeFlag> generalPurposeFlagSet) {
			for (GeneralPurposeFlag flag : generalPurposeFlagSet) {
				generalPurposeFlags |= flag.getValue();
			}
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This overrides the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlagEnums) {
			for (GeneralPurposeFlag flag : generalPurposeFlagEnums) {
				generalPurposeFlags |= flag.getValue();
			}
		}

		public int getCompressionMethod() {
			return compressionMethod;
		}

		public void setCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
		}

		public CompressionMethod getCompressionMethodAsEnum() {
			return CompressionMethod.fromValue(compressionMethod);
		}

		public void setCompressionMethod(CompressionMethod method) {
			this.compressionMethod = method.getValue();
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
			this.crc32 = crc32;
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

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(int uncompressedSize) {
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
