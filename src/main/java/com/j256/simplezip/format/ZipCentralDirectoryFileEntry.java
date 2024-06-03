package com.j256.simplezip.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Set;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.BaseExtraField;
import com.j256.simplezip.format.extra.ExtraFieldUtil;
import com.j256.simplezip.format.extra.Zip64ExtraField;

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
	private final long compressedSize;
	private final long uncompressedSize;
	private final int diskNumberStart;
	private final int internalFileAttributes;
	private final int externalFileAttributes;
	private final int relativeOffsetOfLocalHeader;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;
	private final byte[] commentBytes;
	private final Zip64ExtraField zip64ExtraField;

	public ZipCentralDirectoryFileEntry(int versionMade, int versionNeeded, int generalPurposeFlags,
			int compressionMethod, int lastModifiedTime, int lastModifiedDate, long crc32, long compressedSize,
			long uncompressedSize, int diskNumberStart, int internalFileAttributes, int externalFileAttributes,
			int relativeOffsetOfLocalHeader, byte[] fileNameBytes, byte[] extraFieldBytes, byte[] commentBytes,
			Zip64ExtraField zip64ExtraField) {
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
		this.zip64ExtraField = zip64ExtraField;
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
		builder.crc32 = IoUtils.readIntAsLong(inputStream, "CentralDirectoryFileHeader.crc32");
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

	/**
	 * Extract the platform from the version-made information.
	 */
	public Platform getPlatformMade() {
		return Platform.fromValue((versionMade >> 8) & 0xFF);
	}

	/**
	 * Extract the version portion from the version-made information.
	 */
	public int getVersionMadeMajorMinor() {
		return (versionMade & 0xFF);
	}

	/**
	 * Get the version portion of versionMade as a #.# string.
	 */
	public String getVersionMadeMajorMinorString() {
		int high = (versionMade & 0xFF) / 10;
		int low = (versionMade & 0xFF) % 10;
		return high + "." + low;
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	/**
	 * Get the version needed value as a #.# string.
	 */
	public String getVersionNeededMajorMinorString() {
		int high = (versionNeeded & 0xFF) / 10;
		int low = (versionNeeded & 0xFF) % 10;
		return high + "." + low;
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlagsAsEnums() {
		return GeneralPurposeFlag.fromInt(generalPurposeFlags);
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

	/**
	 * Get the 32-bit size of the compressed (encoded) bytes. This may return 0xFFFFFFFF to indicate that there is a
	 * {@link Zip64ExtraField} in the {@link #getExtraFieldBytes()} that has the real compressed size. See
	 * {@link #getZip64CompressedSize()}.
	 */
	public long getCompressedSize() {
		return compressedSize;
	}

	/**
	 * Get the size of the compressed (unencoded) bytes as encoded in the {@link Zip64ExtraField} which should be in the
	 * {@link #extraFieldBytes}. If there is no extra field then the value of the {@link #getCompressedSize()} is
	 * returned.
	 */
	public long getZip64CompressedSize() {
		if (zip64ExtraField == null) {
			return compressedSize;
		} else {
			return zip64ExtraField.getCompressedSize();
		}
	}

	/**
	 * Get the 32-bit size of the uncompressed, original (unencoded) bytes. This may return 0xFFFFFFFF to indicate that
	 * there is a {@link Zip64ExtraField} in the {@link #getExtraFieldBytes()} that has the real compressed size. See
	 * {@link #getZip64UncompressedSize()}.
	 */
	public long getUncompressedSize() {
		return uncompressedSize;
	}

	/**
	 * Get the size of the uncompressed (unencoded) bytes as encoded in the {@link Zip64ExtraField} which should be in
	 * the {@link #extraFieldBytes}. If there is no extra field then the value of the {@link #getUncompressedSize()} is
	 * returned.
	 */
	public long getZip64UncompressedSize() {
		if (zip64ExtraField == null) {
			return uncompressedSize;
		} else {
			return zip64ExtraField.getUncompressedSize();
		}
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

	/**
	 * Returns the Zip64 extra field in the extra-bytes or null if none.
	 */
	public Zip64ExtraField getZip64ExtraField() {
		return zip64ExtraField;
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
		private int versionNeeded;
		private int generalPurposeFlags;
		private int compressionMethod;
		private int lastModifiedTime;
		private int lastModifiedDate;
		private long crc32;
		private long compressedSize;
		private long uncompressedSize;
		private int diskNumberStart = DEFAULT_DISK_NUMBER;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private int relativeOffsetOfLocalHeader;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;
		private ByteArrayOutputStream extraFieldsOutputStream;
		private byte[] commentBytes;
		private Zip64ExtraField zip64ExtraField;
		private boolean zip64ExtraFieldInBytes;

		public Builder() {
			// XXX: should we calculate this based on features used somehow? Zip64 is v4.5 based for example.
			setVersionMadeMajorMinor(2, 0);
			setPlatformMade(Platform.detectPlatform());
			setVersionMadeMajorMinor(1, 0);
			setExternalFileAttributes(ExternalFileAttributesUtils.UNIX_READ_WRITE_PERMISSIONS);
		}

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
			this.zip64ExtraField = header.getZip64ExtraField();
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
			// build the extra bytes
			byte[] extraBytes;
			if (extraFieldsOutputStream == null && zip64ExtraField == null) {
				extraBytes = extraFieldBytes;
			} else {
				if (extraFieldsOutputStream == null) {
					extraFieldsOutputStream = new ByteArrayOutputStream();
				}
				// we may have extracted the zip64ExtraField from the extraFieldBytes
				if (zip64ExtraField != null && !zip64ExtraFieldInBytes) {
					try {
						zip64ExtraField.write(extraFieldsOutputStream);
					} catch (IOException e) {
						// won't happen with ByteArrayOutputStream
					}
				}
				// tack on the extra field bytes if both were set
				if (extraFieldBytes != null) {
					try {
						extraFieldsOutputStream.write(extraFieldBytes);
					} catch (IOException e) {
						// won't happen with ByteArrayOutputStream
					}
				}
				extraBytes = extraFieldsOutputStream.toByteArray();
			}
			return new ZipCentralDirectoryFileEntry(versionMade, versionNeeded, generalPurposeFlags, compressionMethod,
					lastModifiedTime, lastModifiedDate, crc32, compressedSize, uncompressedSize, diskNumberStart,
					internalFileAttributes, externalFileAttributes, relativeOffsetOfLocalHeader, fileNameBytes,
					extraBytes, commentBytes, zip64ExtraField);
		}

		public int getVersionMade() {
			return versionMade;
		}

		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
		}

		public Platform getPlatformMade() {
			return Platform.fromValue((versionMade >> 8) & 0xFF);
		}

		public void setPlatformMade(Platform platform) {
			this.versionMade = ((this.versionMade & 0xFF) | (platform.getValue() << 8));
		}

		/**
		 * Extract the version portion of the version-made field.
		 */
		public int getVersionMadeMajorMinor() {
			return (versionMade & 0xFF);
		}

		public void setVersionMadeMajorMinor(int major, int minor) {
			this.versionMade = ((this.versionMade & 0xFF00) | (major * 10 + minor));
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public void setVersionNeededMajorMinor(int major, int minor) {
			this.versionNeeded = (major * 10 + minor);
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

		/**
		 * Set to the compressed (encoded) size of the bytes. If this value >= 0xFFFFFFFF then a Zip64 extra field will
		 * be written into the extra bytes if not otherwise specified. You can also set this to 0xFFFFFFFF and add a
		 * {@link Zip64ExtraField} to the {@link #setExtraFieldBytes(byte[])} or
		 * {@link #setZip64ExtraField(Zip64ExtraField)}.
		 */
		public long getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
		}

		public long getUncompressedSize() {
			return uncompressedSize;
		}

		/**
		 * Set to the uncompressed (unencoded) size of the bytes. If this value >= 0xFFFFFFFF then a Zip64 extra field
		 * will be written into the extra bytes if not otherwise specified. You can also set this to 0xFFFFFFFF and add
		 * a {@link Zip64ExtraField} to the {@link #setExtraFieldBytes(byte[])} or
		 * {@link #setZip64ExtraField(Zip64ExtraField)}.
		 */
		public void setUncompressedSize(long uncompressedSize) {
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

		/**
		 * Set the extra-field-bytes.
		 * 
		 * NOTE: This will interrogate the array looking for a {@link Zip64ExtraField}.
		 */
		public void setExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
			if (zip64ExtraField == null) {
				// process the extra bytes looking for an zip64 extra field
				ByteArrayInputStream bais = new ByteArrayInputStream(extraFieldBytes);
				try {
					while (true) {
						BaseExtraField extraField = ExtraFieldUtil.readExtraField(bais, true);
						if (extraField == null) {
							break;
						}
						if (extraField instanceof Zip64ExtraField) {
							this.zip64ExtraField = (Zip64ExtraField) extraField;
							this.zip64ExtraFieldInBytes = true;
							break;
						}
					}
				} catch (IOException e) {
					// could happen with EOF which prolly means an invalid header but oh well
				}
			}
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

		/**
		 * Add an extra field to the header other than the {@link #setZip64ExtraField(Zip64ExtraField)}. You should most
		 * likely either call {@link #setExtraFieldBytes(byte[])} or this method.
		 */
		public Builder addExtraField(BaseExtraField extraField) {
			if (extraFieldsOutputStream == null) {
				extraFieldsOutputStream = new ByteArrayOutputStream();
			}
			try {
				extraField.write(extraFieldsOutputStream);
			} catch (IOException e) {
				// won't happen with byte array output stream
			}
			return this;
		}

		public Zip64ExtraField getZip64ExtraField() {
			return zip64ExtraField;
		}

		public void setZip64ExtraField(Zip64ExtraField zip64ExtraField) {
			this.zip64ExtraField = zip64ExtraField;
		}

		public Builder withZip64ExtraField(Zip64ExtraField zip64ExtraField) {
			this.zip64ExtraField = zip64ExtraField;
			return this;
		}
	}
}
