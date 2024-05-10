package com.j256.simplezip.format;

import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipError;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.ZipStatus;
import com.j256.simplezip.ZipStatus.ZipStatusId;

/**
 * Header of Zip file entries.
 * 
 * @author graywatson
 */
public class ZipFileHeader {

	private static int EXPECTED_SIGNATURE = 0x4034b50;

	private final int signature;
	private final int versionNeeded;
	private final Set<GeneralPurposeFlag> generalPurposeFlags;
	private final int generalPurposeFlagsValue;
	private final int compressionMethodValue;
	private final int lastModFileTime;
	private final int lastModFileDate;
	private final int crc32;
	private final int compressedSize;
	private final int uncompressedSize;
	private final byte[] fileNameBytes;
	private final byte[] extraBytes;

	public ZipFileHeader(int signature, int versionNeeded, int generalPurposeFlagsValue, int compressionMethod,
			int lastModFileTime, int lastModFileDate, int crc32, int compressedSize, int uncompressedSize,
			byte[] fileName, byte[] extra) {
		this.signature = signature;
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = GeneralPurposeFlag.fromInt(generalPurposeFlagsValue);
		this.generalPurposeFlagsValue = generalPurposeFlagsValue;
		this.compressionMethodValue = compressionMethod;
		this.lastModFileTime = lastModFileTime;
		this.lastModFileDate = lastModFileDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.fileNameBytes = fileName;
		this.extraBytes = extra;
	}

	public static ZipFileHeader read(RewindableInputStream input) throws IOException {
		Builder builder = new ZipFileHeader.Builder();
		/*
		 * WHen reading a file-header we aren't sure if this is a file-header or the start of the central directory.
		 */
		int first = IoUtils.readInt(input, "LocalFileHeader.signature");
		if (first == CentralDirectoryFileHeader.EXPECTED_SIGNATURE) {
			input.rewind(4);
			return null;
		}
		builder.setSignature(first);
		builder.versionNeeded = IoUtils.readShort(input, "LocalFileHeader.versionNeeded");
		builder.generalPurposeFlagsValue = IoUtils.readShort(input, "LocalFileHeader.generalPurposeFlags");
		builder.compressionMethodValue = IoUtils.readShort(input, "LocalFileHeader.compressionMethod");
		builder.lastModFileTime = IoUtils.readShort(input, "LocalFileHeader.lastModFileTime");
		builder.lastModFileDate = IoUtils.readShort(input, "LocalFileHeader.lastModFileDate");
		builder.crc32 = IoUtils.readInt(input, "LocalFileHeader.crc32");
		builder.compressedSize = IoUtils.readInt(input, "LocalFileHeader.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(input, "LocalFileHeader.uncompressedSize");
		int fileNameLength = IoUtils.readShort(input, "LocalFileHeader.fileNameLength");
		int extraLength = IoUtils.readShort(input, "LocalFileHeader.extraLength");
		builder.fileNameBytes = IoUtils.readBytes(input, "LocalFileHeader.fileName", fileNameLength);
		builder.extraBytes = IoUtils.readBytes(input, "LocalFileHeader.extra", extraLength);
		return builder.build();
	}

	/**
	 * Validate the returned header.
	 * 
	 * @return null if there were no problems otherwise a {@link ZipError}.
	 */
	public ZipStatus validate() {
		if (signature != EXPECTED_SIGNATURE) {
			return new ZipStatus(ZipStatusId.HEADER_BAD_SIGNATURE, "file header is " + Integer.toHexString(signature)
					+ ", expecting " + Integer.toHexString(EXPECTED_SIGNATURE));
		} else {
			return null;
		}
	}

	/**
	 * Return whether the header has this flag.
	 */
	public boolean hasFlag(GeneralPurposeFlag flag) {
		return generalPurposeFlags.contains(flag);
	}

	public int getSignature() {
		return signature;
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

	public int getLastModFileTime() {
		return lastModFileTime;
	}

	public int getLastModFileDate() {
		return lastModFileDate;
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

	public byte[] getFileNameBytes() {
		return fileNameBytes;
	}

	public String getFileName() {
		return new String(fileNameBytes);
	}

	public byte[] getExtra() {
		return extraBytes;
	}

	/**
	 * Builder for the LocalFileHeader class.
	 */
	public static class Builder {
		private int signature;
		private int versionNeeded;
		private int generalPurposeFlagsValue;
		private int compressionMethodValue;
		private int lastModFileTime;
		private int lastModFileDate;
		private int crc32;
		private int compressedSize;
		private int uncompressedSize;
		private byte[] fileNameBytes;
		private byte[] extraBytes;

		public ZipFileHeader build() {
			return new ZipFileHeader(signature, versionNeeded, generalPurposeFlagsValue, compressionMethodValue,
					lastModFileTime, lastModFileDate, crc32, compressedSize, uncompressedSize, fileNameBytes,
					extraBytes);
		}

		public int getSignature() {
			return signature;
		}

		public void setSignature(int signature) {
			this.signature = signature;
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
			generalPurposeFlagsValue = 0;
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

		public int getLastModFileTime() {
			return lastModFileTime;
		}

		public void setLastModFileTime(int lastModFileTime) {
			this.lastModFileTime = lastModFileTime;
		}

		public int getLastModFileDate() {
			return lastModFileDate;
		}

		public void setLastModFileDate(int lastModFileDate) {
			this.lastModFileDate = lastModFileDate;
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

		public byte[] getFileNameBytes() {
			return fileNameBytes;
		}

		public void setFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
		}

		public byte[] getExtraBytes() {
			return extraBytes;
		}

		public void setExtraBytes(byte[] extra) {
			this.extraBytes = extra;
		}
	}
}
