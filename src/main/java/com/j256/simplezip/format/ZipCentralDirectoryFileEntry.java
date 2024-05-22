package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * File headers stored in the central directory.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryFileEntry {

	/** signature that is expected to be at the start of the central directory */
	private static final int EXPECTED_SIGNATURE = 0x2014b50;
	public static final int INTERNAL_ATTRIBUTES_TEXT_FILE = (1 << 0);
	public static final int DEFAULT_DISK_NUMBER = 0;
	/** This is the minimum size that this header will take on disk. */
	public static final int MINIMUM_READ_SIZE = 6 * 2 + 3 * 4 + 5 * 2 + 2 * 4;

	private final int versionMade;
	private final int versionNeeded;
	private final int generalPurposeFlags;
	private final int compressionMethod;
	private final int lastModifiedTime;
	private final int lastModifiedDate;
	private final long crc32;
	private final int compressedSize;
	private final int uncompressedSize;
	private final int diskNumberStart;
	private final int internalFileAttributes;
	private final int externalFileAttributes;
	private final int relativeOffsetOfLocalHeader;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;
	private final byte[] commentBytes;

	public ZipCentralDirectoryFileEntry(int versionMade, int versionNeeded, int generalPurposeFlags,
			int compressionMethod, int lastModifiedTime, int lastModifiedDate, long crc32, int compressedSize,
			int uncompressedSize, int diskNumberStart, int internalFileAttributes, int externalFileAttributes,
			int relativeOffsetOfLocalHeader, byte[] fileNameBytes, byte[] extraFieldBytes, byte[] commentBytes) {
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = generalPurposeFlags;
		this.compressionMethod = compressionMethod;
		this.lastModifiedTime = lastModifiedTime;
		this.lastModifiedDate = lastModifiedDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.diskNumberStart = diskNumberStart;
		this.internalFileAttributes = internalFileAttributes;
		this.externalFileAttributes = externalFileAttributes;
		this.relativeOffsetOfLocalHeader = relativeOffsetOfLocalHeader;
		this.fileNameBytes = fileNameBytes;
		this.extraFieldBytes = extraFieldBytes;
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
	public static ZipCentralDirectoryFileEntry read(RewindableInputStream inputStream) throws IOException {

		int signature = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.signature");

		if (signature != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}

		Builder builder = new ZipCentralDirectoryFileEntry.Builder();
		builder.versionMade = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.versionMade");
		builder.versionNeeded = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.versionNeeded");
		builder.generalPurposeFlags = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.generalPurposeFlags");
		builder.compressionMethod = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.compressionMethod");
		builder.lastModifiedTime = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.lastModifiedTime");
		builder.lastModifiedDate = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.lastModifiedDate");
		builder.crc32 = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.crc32");
		builder.compressedSize = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.uncompressedSize");
		int fileNameLength = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.fileNameLength");
		int extraFieldLength = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.extraFieldLength");
		int commentLength = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.commentLength");
		builder.diskNumberStart = IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.diskNumberStart");
		builder.internalFileAttributes =
				IoUtils.readShort(inputStream, "CentralDirectoryFileHeader.internalFileAttributes");
		builder.externalFileAttributes =
				IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.externalFileAttributes");
		builder.relativeOffsetOfLocalHeader =
				IoUtils.readInt(inputStream, "CentralDirectoryFileHeader.relativeOffsetOfLocalHeader");

		builder.fileNameBytes = IoUtils.readBytes(inputStream, fileNameLength, "CentralDirectoryFileHeader.fileName");
		builder.extraFieldBytes =
				IoUtils.readBytes(inputStream, extraFieldLength, "CentralDirectoryFileHeader.extraField");
		builder.commentBytes = IoUtils.readBytes(inputStream, commentLength, "CentralDirectoryFileHeader.comment");

		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {

		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeShort(outputStream, versionMade);
		IoUtils.writeShort(outputStream, versionNeeded);
		IoUtils.writeShort(outputStream, generalPurposeFlags);
		IoUtils.writeShort(outputStream, compressionMethod);
		IoUtils.writeShort(outputStream, lastModifiedTime);
		IoUtils.writeShort(outputStream, lastModifiedDate);
		IoUtils.writeInt(outputStream, crc32);
		IoUtils.writeInt(outputStream, compressedSize);
		IoUtils.writeInt(outputStream, uncompressedSize);

		IoUtils.writeShortBytesLength(outputStream, fileNameBytes);
		IoUtils.writeShortBytesLength(outputStream, extraFieldBytes);
		IoUtils.writeShortBytesLength(outputStream, commentBytes);
		IoUtils.writeShort(outputStream, diskNumberStart);
		IoUtils.writeShort(outputStream, internalFileAttributes);
		IoUtils.writeInt(outputStream, externalFileAttributes);
		IoUtils.writeInt(outputStream, relativeOffsetOfLocalHeader);
		IoUtils.writeBytes(outputStream, fileNameBytes);
		IoUtils.writeBytes(outputStream, extraFieldBytes);
		IoUtils.writeBytes(outputStream, commentBytes);
	}

	public int getVersionMade() {
		return versionMade;
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	/**
	 * Extract the platform from the version-made information.
	 */
	public Platform getPlatform() {
		return Platform.fromValue((versionMade >> 8) & 0xFF);
	}

	/**
	 * Extract the needed version from the version-made information.
	 */
	public ZipVersion getZipVersion() {
		return ZipVersion.fromValue(versionMade & 0xFF);
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
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

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public int getInternalFileAttributes() {
		return internalFileAttributes;
	}

	/**
	 * Return whether this is a text file or not based on the internalFileAttributes.
	 */
	public boolean isTextFile() {
		return ((internalFileAttributes & INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
	}

	public int getExternalFileAttributes() {
		return externalFileAttributes;
	}

	public int getRelativeOffsetOfLocalHeader() {
		return relativeOffsetOfLocalHeader;
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

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	public String getComment() {
		if (commentBytes == null) {
			return null;
		} else {
			return new String(commentBytes);
		}
	}

	@Override
	public String toString() {
		return "CentralDirectoryFileHeader [fileName=" + (fileNameBytes == null ? "null" : new String(fileNameBytes))
				+ ", method=" + compressionMethod + ", compSize=" + compressedSize + ", uncompSize=" + uncompressedSize
				+ "]";
	}

	/**
	 * Builder for the {@link ZipCentralDirectoryFileEntry}.
	 */
	public static class Builder {
		private int versionMade;
		private int versionNeeded = ZipVersion.detectVersion().getValue();
		private int generalPurposeFlags;
		private int compressionMethod;
		private int lastModifiedTime;
		private int lastModifiedDate;
		private long crc32;
		private int compressedSize;
		private int uncompressedSize;
		private int diskNumberStart = DEFAULT_DISK_NUMBER;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private int relativeOffsetOfLocalHeader;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;
		private byte[] commentBytes;

		/**
		 * Create a builder from an existing directory-end
		 */
		public static Builder fromFileHeader(ZipCentralDirectoryFileEntry header) {
			Builder builder = new Builder();
			builder.versionMade = header.versionMade;
			builder.versionNeeded = header.versionNeeded;
			builder.generalPurposeFlags = header.generalPurposeFlags;
			builder.compressionMethod = header.compressionMethod;
			builder.lastModifiedTime = header.lastModifiedTime;
			builder.lastModifiedDate = header.lastModifiedDate;
			builder.crc32 = header.crc32;
			builder.compressedSize = header.compressedSize;
			builder.uncompressedSize = header.uncompressedSize;
			builder.diskNumberStart = header.diskNumberStart;
			builder.internalFileAttributes = header.internalFileAttributes;
			builder.externalFileAttributes = header.externalFileAttributes;
			builder.relativeOffsetOfLocalHeader = header.relativeOffsetOfLocalHeader;
			builder.fileNameBytes = header.fileNameBytes;
			builder.extraFieldBytes = header.extraFieldBytes;
			builder.commentBytes = header.commentBytes;
			return builder;
		}

		/**
		 * Create a builder from an existing file-header.
		 */
		public void setFileHeader(ZipFileHeader header) {
			this.generalPurposeFlags = header.getGeneralPurposeFlags();
			this.compressionMethod = header.getCompressionMethod();
			this.lastModifiedTime = header.getLastModifiedTime();
			this.lastModifiedDate = header.getLastModifiedDate();
			this.crc32 = header.getCrc32();
			this.compressedSize = header.getCompressedSize();
			this.uncompressedSize = header.getUncompressedSize();
			this.fileNameBytes = header.getFileNameBytes();
			this.extraFieldBytes = header.getExtraFieldBytes();
		}

		/**
		 * Reset the builder in case you want to reuse. This does set a couple of default fields.
		 */
		public void reset() {
			versionMade = 0;
			versionNeeded = ZipVersion.detectVersion().getValue();
			generalPurposeFlags = 0;
			compressionMethod = 0;
			lastModifiedTime = 0;
			lastModifiedDate = 0;
			crc32 = 0;
			compressedSize = 0;
			uncompressedSize = 0;
			diskNumberStart = DEFAULT_DISK_NUMBER;
			internalFileAttributes = 0;
			externalFileAttributes = 0;
			relativeOffsetOfLocalHeader = 0;
			fileNameBytes = null;
			extraFieldBytes = null;
			commentBytes = null;
		}

		/**
		 * Add to this builder the additional file information.
		 */
		public void addFileInfo(ZipCentralDirectoryFileInfo fileInfo) {
			this.versionMade = fileInfo.getVersionMade();
			this.versionNeeded = fileInfo.getVersionNeeded();
			this.diskNumberStart = fileInfo.getDiskNumberStart();
			this.internalFileAttributes = fileInfo.getInternalFileAttributes();
			this.externalFileAttributes = fileInfo.getExternalFileAttributes();
			this.commentBytes = fileInfo.getCommentBytes();
		}

		/**
		 * Builder an instance of the central-directory file-header.
		 */
		public ZipCentralDirectoryFileEntry build() {
			return new ZipCentralDirectoryFileEntry(versionMade, versionNeeded, generalPurposeFlags, compressionMethod,
					lastModifiedTime, lastModifiedDate, crc32, compressedSize, uncompressedSize, diskNumberStart,
					internalFileAttributes, externalFileAttributes, relativeOffsetOfLocalHeader, fileNameBytes,
					extraFieldBytes, commentBytes);
		}

		public int getVersionMade() {
			return versionMade;
		}

		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
		}

		public Platform getPlatform() {
			return Platform.fromValue((versionMade >> 8) & 0xFF);
		}

		public void setPlatform(Platform platform) {
			this.versionMade = ((this.versionMade & 0xFF) | (platform.getValue() << 8));
		}

		public ZipVersion getZipVersion() {
			return ZipVersion.fromValue(versionMade & 0xFF);
		}

		public void setZipVersion(ZipVersion version) {
			this.versionMade = ((this.versionMade & 0xFF00) | version.getValue());
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

		public int getCompressionMethod() {
			return compressionMethod;
		}

		public void setCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
		}

		public CompressionMethod getCompressionMethodAsEnum() {
			return CompressionMethod.fromValue(compressionMethod);
		}

		public void setCompressionMethod(CompressionMethod compressionMethod) {
			this.compressionMethod = compressionMethod.getValue();
		}

		public int getLastModifiedTime() {
			return lastModifiedTime;
		}

		public void setLastModifiedTime(int lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}

		public int getLastModifiedDate() {
			return lastModifiedDate;
		}

		public void setLastModifiedDate(int lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
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

		public int getDiskNumberStart() {
			return diskNumberStart;
		}

		public void setDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
		}

		public int getInternalFileAttributes() {
			return internalFileAttributes;
		}

		public void setInternalFileAttributes(int internalFileAttributes) {
			this.internalFileAttributes = internalFileAttributes;
		}

		/**
		 * Gets from the internalFileAttributes.
		 */
		public boolean isTextFile() {
			return ((internalFileAttributes & INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
		}

		/**
		 * Set in the internalFileAttributes.
		 */
		public void setTextFile(boolean textFile) {
			if (textFile) {
				internalFileAttributes |= INTERNAL_ATTRIBUTES_TEXT_FILE;
			} else {
				internalFileAttributes &= ~INTERNAL_ATTRIBUTES_TEXT_FILE;
			}
		}

		public int getExternalFileAttributes() {
			return externalFileAttributes;
		}

		public void setExternalFileAttributes(int externalFileAttributes) {
			this.externalFileAttributes = externalFileAttributes;
		}

		public int getRelativeOffsetOfLocalHeader() {
			return relativeOffsetOfLocalHeader;
		}

		public void setRelativeOffsetOfLocalHeader(int relativeOffsetOfLocalHeader) {
			this.relativeOffsetOfLocalHeader = relativeOffsetOfLocalHeader;
		}

		public byte[] getFileNameBytes() {
			return fileNameBytes;
		}

		public void setFileNameBytes(byte[] fileNameBytes) {
			this.fileNameBytes = fileNameBytes;
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

		public void setExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
		}

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}

		public String getComment() {
			if (commentBytes == null) {
				return null;
			} else {
				return new String(commentBytes);
			}
		}

		public void setComment(String comment) {
			if (comment == null) {
				commentBytes = null;
			} else {
				commentBytes = comment.getBytes();
			}
		}
	}
}
