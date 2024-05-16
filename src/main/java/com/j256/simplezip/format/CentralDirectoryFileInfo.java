package com.j256.simplezip.format;

import java.io.File;

/**
 * Additional file information that can be written into the central-directory that does not exist in the
 * {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public class CentralDirectoryFileInfo {

	private int versionMade;
	private int versionNeeded;
	private int diskNumberStart;
	private int internalFileAttributes;
	private int externalFileAttributes;
	private final byte[] commentBytes;

	public CentralDirectoryFileInfo(int versionMade, int versionNeeded, int diskNumberStart, int internalFileAttributes,
			int externalFileAttributes, byte[] commentBytes) {
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.diskNumberStart = diskNumberStart;
		this.internalFileAttributes = internalFileAttributes;
		this.externalFileAttributes = externalFileAttributes;
		this.commentBytes = commentBytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
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
		return ((internalFileAttributes & CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
	}

	public int getExternalFileAttributes() {
		return externalFileAttributes;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	public String getComment() {
		return new String(commentBytes);
	}

	/**
	 * Builder for the {@link CentralDirectoryFileInfo}.
	 */
	public static class Builder {
		private int versionMade;
		private int versionNeeded;
		private int diskNumberStart = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private byte[] commentBytes;

		public Builder() {
			// detect and set the platform and version automatically
			setPlatform(Platform.detectPlatform());
			setZipVersion(ZipVersion.detectVersion());
		}

		/**
		 * Create a builder from an existing central-directory file-header.
		 */
		public static Builder fromFileHeader(CentralDirectoryFileHeader header) {
			Builder builder = new Builder();
			builder.versionMade = header.getVersionMade();
			builder.versionNeeded = header.getVersionNeeded();
			builder.diskNumberStart = header.getDiskNumberStart();
			builder.internalFileAttributes = header.getInternalFileAttributes();
			builder.externalFileAttributes = header.getExternalFileAttributes();
			builder.commentBytes = header.getCommentBytes();
			return builder;
		}

		/**
		 * Builder an instance of the central-directory file-header.
		 */
		public CentralDirectoryFileInfo build() {
			return new CentralDirectoryFileInfo(versionMade, versionNeeded, diskNumberStart, internalFileAttributes,
					externalFileAttributes, commentBytes);
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
			return ((internalFileAttributes & CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
		}

		/**
		 * Set in the internalFileAttributes.
		 */
		public void setTextFile(boolean textFile) {
			if (textFile) {
				internalFileAttributes |= CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE;
			} else {
				internalFileAttributes &= ~CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE;
			}
		}

		public int getExternalFileAttributes() {
			return externalFileAttributes;
		}

		public void setExternalFileAttributes(int externalFileAttributes) {
			this.externalFileAttributes = externalFileAttributes;
		}

		/**
		 * Set the externalFileAttributes from the attributes associated with the file argument.
		 */
		public void setExternalFileAttributesFromFile(File file) {
			this.externalFileAttributes = FilePermissions.fromFile(file);
		}

		/**
		 * Set the Unix file mode into the top of the externalFileAttributes. For example you can set this with 0644 or
		 * 0777.
		 */
		public void setUnixExternalFileAttributes(int unixFileAttributes) {
			externalFileAttributes = ((externalFileAttributes & 0xFFFF) | (unixFileAttributes << 16));
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a directory.
		 */
		public void setFileIsDirectory(boolean isDirectory) {
			if (isDirectory) {
				externalFileAttributes |= FilePermissions.UNIX_DIRECTORY;
				externalFileAttributes |= FilePermissions.MS_DOS_DIRECTORY;
			} else {
				externalFileAttributes &= ~FilePermissions.UNIX_DIRECTORY;
				externalFileAttributes &= ~FilePermissions.MS_DOS_DIRECTORY;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a symbolic-link.
		 */
		public void setFileIsSymlink(boolean isSymlink) {
			if (isSymlink) {
				externalFileAttributes |= FilePermissions.UNIX_SYMLINK;
			} else {
				externalFileAttributes &= ~FilePermissions.UNIX_SYMLINK;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a regular-file.
		 */
		public void setFileIsRegular(boolean isDirectory) {
			if (isDirectory) {
				externalFileAttributes |= FilePermissions.UNIX_REGULAR_FILE;
			} else {
				externalFileAttributes &= ~FilePermissions.UNIX_REGULAR_FILE;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is read-only.
		 */
		public void setFileReadOnly(boolean readOnly) {
			if (readOnly) {
				externalFileAttributes =
						((externalFileAttributes & 0xFFFF) | FilePermissions.UNIX_READ_ONLY_PERMISSIONS);
				externalFileAttributes |= FilePermissions.MS_DOS_READONLY;
			} else {
				externalFileAttributes &= ~FilePermissions.MS_DOS_READONLY;
			}
		}

		/**
		 * Set the MS-DOS file mode into the bottom of the externalFileAttributes.
		 */
		public void setMsDosExternalFileAttributes(int msDosFileAttributes) {
			externalFileAttributes = ((externalFileAttributes & 0xFF) | msDosFileAttributes);
		}

		/**
		 * Set the external
		 */
		public void setExternalAttributesFromFile(File file) {
			externalFileAttributes = FilePermissions.fromFile(file);
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
