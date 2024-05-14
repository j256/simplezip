package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
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
	private final Set<GeneralPurposeFlag> generalPurposeFlagEnums;
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
		this.generalPurposeFlagEnums = GeneralPurposeFlag.fromInt(generalPurposeFlags);
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
		IoUtils.writeShort(outputStream, generalPurposeFlags);
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
		return generalPurposeFlagEnums.contains(flag);
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlagAsEnums() {
		return generalPurposeFlagEnums;
	}

	/**
	 * Read the compression level from that flags.
	 */
	public int getCompressionLevel() {
		int level = Deflater.DEFAULT_COMPRESSION;
		for (GeneralPurposeFlag flag : GeneralPurposeFlag.fromInt(compressionMethod)) {
			switch (flag) {
				case DEFLATING_MAXIMUM:
					return Deflater.BEST_COMPRESSION;
				case DEFLATING_NORMAL:
					return Deflater.DEFAULT_COMPRESSION;
				case DEFLATING_FAST:
					// i guess this is right
					return Deflater.DEFAULT_COMPRESSION + Deflater.BEST_SPEED / 2;
				case DEFLATING_SUPER_FAST:
					return Deflater.BEST_SPEED;
				default:
					// not a compression speed thing
					break;
			}
		}
		return level;
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
		private int generalPurposeFlags = GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
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
			Builder builder = new Builder();
			builder.versionNeeded = 0;
			builder.generalPurposeFlags = 0;
			builder.compressionMethod = 0;
			builder.lastModifiedFileTime = 0;
			builder.lastModifiedFileDate = 0;
			builder.crc32 = 0;
			builder.compressedSize = 0;
			builder.uncompressedSize = 0;
			builder.fileNameBytes = null;
			builder.extraFieldBytes = null;
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
		 * Adds to the current general-purpose-flags the value associated with this flag enum.
		 */
		public void addGeneralPurposeFlag(GeneralPurposeFlag flag) {
			this.generalPurposeFlags |= flag.getValue();
		}

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
			if (crc32 != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlags &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
			}
			this.crc32 = crc32;
		}

		public int getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(int compressedSize) {
			if (compressedSize != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlags &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
			}
			this.compressedSize = compressedSize;
		}

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(int uncompressedSize) {
			if (uncompressedSize != 0) {
				// turn off the data-descriptor if we have a crc or size
				generalPurposeFlags &= ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
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
