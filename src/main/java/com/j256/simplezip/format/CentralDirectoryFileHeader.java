package com.j256.simplezip.format;

import java.io.IOException;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * File headers stored in the central directory.
 * 
 * @author graywatson
 */
public class CentralDirectoryFileHeader {

	/** signature that is expected to be at the start of the central directory */
	public static final int EXPECTED_SIGNATURE = 0x2014b50;
	private static final int INTERNAL_ATTRIBUTES_TEXT_FILE = (1 << 0);

	private int signature;
	private int versionMade;
	private int versionNeeded;
	private int generalPurposeFlags;
	private int compressionMethodValue;
	private int lastModifiedFileTime;
	private int lastModifiedFileDate;
	private int crc32;
	private int compressedSize;
	private int uncompressedSize;
	private int diskNumberStart;
	private int internalFileAttributes;
	private int externalFileAttributes;
	private int relativeOffsetOfLocalHeader;
	private final byte[] fileNameBytes;
	private byte[] extraFieldBytes;
	private final byte[] commentBytes;

	public CentralDirectoryFileHeader(int signature, int versionMade, int versionNeeded, int generalPurposeFlags,
			int compressionMethodValue, int lastModifiedFileTime, int lastModifiedFileDate, int crc32,
			int compressedSize, int uncompressedSize, int diskNumberStart, int internalFileAttributes,
			int externalFileAttributes, int relativeOffsetOfLocalHeader, byte[] fileNameBytes, byte[] extraFieldBytes,
			byte[] commentBytes) {
		this.signature = signature;
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = generalPurposeFlags;
		this.compressionMethodValue = compressionMethodValue;
		this.lastModifiedFileTime = lastModifiedFileTime;
		this.lastModifiedFileDate = lastModifiedFileDate;
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

	public static CentralDirectoryFileHeader read(RewindableInputStream input) throws IOException {

		Builder builder = new CentralDirectoryFileHeader.Builder();

		int signature = IoUtils.readInt(input, "CentralDirectoryFileHeader.signature");
		if (signature < 0) {
			return null;
		}

		if (signature == CentralDirectoryEnd.EXPECTED_SIGNATURE) {
			input.rewind(4);
			return null;
		}

		builder.signature = signature;
		builder.versionMade = IoUtils.readShort(input, "CentralDirectoryFileHeader.versionMade");
		builder.versionNeeded = IoUtils.readShort(input, "CentralDirectoryFileHeader.versionNeeded");
		builder.generalPurposeFlags = IoUtils.readShort(input, "CentralDirectoryFileHeader.generalPurposeFlags");
		builder.compressionMethodValue = IoUtils.readShort(input, "CentralDirectoryFileHeader.compressionMethod");
		builder.lastModifiedFileTime = IoUtils.readShort(input, "CentralDirectoryFileHeader.lastModifiedFileTime");
		builder.lastModifiedFileDate = IoUtils.readShort(input, "CentralDirectoryFileHeader.lastModifiedFileDate");
		builder.crc32 = IoUtils.readInt(input, "CentralDirectoryFileHeader.crc32");
		builder.compressedSize = IoUtils.readInt(input, "CentralDirectoryFileHeader.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(input, "CentralDirectoryFileHeader.uncompressedSize");
		int fileNameLength = IoUtils.readShort(input, "CentralDirectoryFileHeader.fileNameLength");
		int extraFieldLength = IoUtils.readShort(input, "CentralDirectoryFileHeader.extraFieldLength");
		int commentLength = IoUtils.readShort(input, "CentralDirectoryFileHeader.commentLength");
		builder.diskNumberStart = IoUtils.readShort(input, "CentralDirectoryFileHeader.diskNumberStart");
		builder.internalFileAttributes = IoUtils.readShort(input, "CentralDirectoryFileHeader.internalFileAttributes");
		builder.externalFileAttributes = IoUtils.readInt(input, "CentralDirectoryFileHeader.externalFileAttributes");
		builder.relativeOffsetOfLocalHeader =
				IoUtils.readInt(input, "CentralDirectoryFileHeader.relativeOffsetOfLocalHeader");

		builder.fileNameBytes = IoUtils.readBytes(input, fileNameLength, "CentralDirectoryFileHeader.fileName");
		builder.extraFieldBytes = IoUtils.readBytes(input, extraFieldLength, "CentralDirectoryFileHeader.extraField");
		builder.commentBytes = IoUtils.readBytes(input, commentLength, "CentralDirectoryFileHeader.comment");

		return builder.build();
	}

	public int getSignature() {
		return signature;
	}

	public int getVersionMade() {
		return versionMade;
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getGeneralPurposeFlags() {
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

	public int getLastModifiedFileDate() {
		return lastModifiedFileDate;
	}

	public int getCrc32() {
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
		return new String(fileNameBytes);
	}

	public byte[] getExtraFieldBytes() {
		return extraFieldBytes;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	public String getComment() {
		return new String(commentBytes);
	}

	/**
	 * Builder for the {@link CentralDirectoryFileHeader}.
	 */
	public static class Builder {
		private int signature;
		private int versionMade;
		private int versionNeeded;
		private int generalPurposeFlags;
		private int compressionMethodValue;
		private int lastModifiedFileTime;
		private int lastModifiedFileDate;
		private int crc32;
		private int compressedSize;
		private int uncompressedSize;
		private int diskNumberStart;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private int relativeOffsetOfLocalHeader;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;
		private byte[] commentBytes;

		public int getSignature() {
			return signature;
		}

		public void setSignature(int signature) {
			this.signature = signature;
		}

		public int getVersionMade() {
			return versionMade;
		}

		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
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

		public int getCompressionMethodValue() {
			return compressionMethodValue;
		}

		public void setCompressionMethodValue(int compressionMethodValue) {
			this.compressionMethodValue = compressionMethodValue;
		}

		public CompressionMethod getCompressionMethod() {
			return CompressionMethod.fromValue(compressionMethodValue);
		}

		public void setCompressionMethodValue(CompressionMethod compressionMethod) {
			this.compressionMethodValue = compressionMethod.getValue();
		}

		public int getLastModifiedFileTime() {
			return lastModifiedFileTime;
		}

		public void setLastModifiedFileTime(int lastModifiedFileTime) {
			this.lastModifiedFileTime = lastModifiedFileTime;
		}

		public int getLastModifiedFileDate() {
			return lastModifiedFileDate;
		}

		public void setLastModifiedFileDate(int lastModifiedFileDate) {
			this.lastModifiedFileDate = lastModifiedFileDate;
		}

		public int getCrc32() {
			return crc32;
		}

		public void setCrc32(int crc32) {
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

		public CentralDirectoryFileHeader build() {
			return new CentralDirectoryFileHeader(signature, versionMade, versionNeeded, generalPurposeFlags,
					compressionMethodValue, lastModifiedFileTime, lastModifiedFileDate, crc32, compressedSize,
					uncompressedSize, diskNumberStart, internalFileAttributes, externalFileAttributes,
					relativeOffsetOfLocalHeader, fileNameBytes, extraFieldBytes, commentBytes);
		}
	}

}
