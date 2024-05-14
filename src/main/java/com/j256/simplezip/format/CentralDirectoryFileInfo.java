package com.j256.simplezip.format;

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
		private int versionNeeded = ZipVersion.detectVersion().getValue();
		private int diskNumberStart = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private byte[] commentBytes;

		/**
		 * Create a builder from an existing directory-end
		 */
		public static Builder fromEnd(CentralDirectoryFileInfo header) {
			Builder builder = new Builder();
			builder.versionMade = header.versionMade;
			builder.versionNeeded = header.versionNeeded;
			builder.diskNumberStart = header.diskNumberStart;
			builder.internalFileAttributes = header.internalFileAttributes;
			builder.externalFileAttributes = header.externalFileAttributes;
			builder.commentBytes = header.commentBytes;
			return builder;
		}

		/**
		 * Reset the builder in case you want to reuse.
		 */
		public void reset() {
			versionMade = 0;
			versionNeeded = ZipVersion.detectVersion().getValue();
			diskNumberStart = CentralDirectoryFileHeader.DEFAULT_DISK_NUMBER;
			internalFileAttributes = 0;
			externalFileAttributes = 0;
			commentBytes = null;
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

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}
	}
}
