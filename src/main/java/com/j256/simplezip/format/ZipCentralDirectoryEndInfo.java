package com.j256.simplezip.format;

/**
 * Additional information that can be written at the very end of the zip file in a {@link ZipCentralDirectoryEnd}
 * structure.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryEndInfo {

	private final boolean zip64;
	private final int versionMade;
	private final int versionNeeded;
	private final int diskNumber;
	private final int diskNumberStart;
	private final byte[] commentBytes;
	private final byte[] extensibleData;
	private final int numberDisks;

	public ZipCentralDirectoryEndInfo(boolean zip64, int versionMade, int versionNeeded, int diskNumber,
			int diskNumberStart, byte[] commentBytes, byte[] extensibleData, int numberDisks) {
		this.zip64 = zip64;
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.commentBytes = commentBytes;
		this.extensibleData = extensibleData;
		this.numberDisks = numberDisks;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Whether we should write a zip64 end with a end-locator or just the noraml end block.
	 */
	public boolean isZip64() {
		return zip64;
	}

	/**
	 * Version made field for the Zip64 end entry.
	 */
	public int getVersionMade() {
		return versionMade;
	}

	/**
	 * Version needed field for the Zip64 end entry.
	 */
	public int getVersionNeeded() {
		return versionNeeded;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	/**
	 * Get the comment for the Zip32 format end.
	 */
	public byte[] getCommentBytes() {
		return commentBytes;
	}

	/**
	 * Get the comment for the Zip32 format end.
	 */
	public String getComment() {
		if (commentBytes == null) {
			return null;
		} else {
			return new String(commentBytes);
		}
	}

	/**
	 * Extensible data needed field for the Zip64 end entry.
	 */
	public byte[] getExtensibleData() {
		return extensibleData;
	}

	/**
	 * Get the comment for the Zip64 format end.
	 */
	public int getNumberDisks() {
		return numberDisks;
	}

	/**
	 * Builder for the {@link ZipCentralDirectoryEndInfo}.
	 */
	public static class Builder {
		private boolean zip64;
		private int versionMade;
		private int versionNeeded;
		private int diskNumber = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private byte[] commentBytes;
		private byte[] extensibleData;
		private int numberDisks = 1;

		/**
		 * Create a builder from an existing central-directory file-entry.
		 */
		public static Builder fromCentralDirectoryEnd(ZipCentralDirectoryEnd end) {
			Builder builder = new Builder();
			builder.zip64 = end.isZip64();
			builder.versionMade = end.getVersionMade();
			builder.versionNeeded = end.getVersionNeeded();
			builder.diskNumber = end.getDiskNumber();
			builder.diskNumberStart = end.getDiskNumberStart();
			builder.commentBytes = end.getCommentBytes();
			builder.extensibleData = end.getExtensibleData();
			return builder;
		}

		/**
		 * Builder an instance of the central-directory file-header.
		 */
		public ZipCentralDirectoryEndInfo build() {
			return new ZipCentralDirectoryEndInfo(zip64, versionMade, versionNeeded, diskNumber, diskNumberStart,
					commentBytes, extensibleData, numberDisks);
		}

		/**
		 * Whether or not we should write a zip64 end block.
		 */
		public boolean isZip64() {
			return zip64;
		}

		/**
		 * Whether or not we should write a zip64 end block.
		 */
		public void setZip64(boolean zip64) {
			this.zip64 = zip64;
		}

		/**
		 * Whether or not we should write a zip64 end block.
		 */
		public Builder withZip64(boolean zip64) {
			this.zip64 = zip64;
			return this;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public int getVersionMade() {
			return versionMade;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public Builder withVersionMade(int versionMade) {
			this.versionMade = versionMade;
			return this;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public int getVersionNeeded() {
			return versionNeeded;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public Builder withVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
			return this;
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}

		public Builder withDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
			return this;
		}

		public int getDiskNumberStart() {
			return diskNumberStart;
		}

		public void setDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
		}

		public Builder withDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
			return this;
		}

		/**
		 * For Zip32 end information.
		 */
		public byte[] getCommentBytes() {
			return commentBytes;
		}

		/**
		 * For Zip32 end information.
		 */
		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}

		/**
		 * For Zip32 end information.
		 */
		public Builder withCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
			return this;
		}

		/**
		 * For Zip32 end information.
		 */
		public String getComment() {
			if (commentBytes == null) {
				return null;
			} else {
				return new String(commentBytes);
			}
		}

		/**
		 * For Zip32 end information.
		 */
		public void setComment(String comment) {
			if (comment == null) {
				commentBytes = null;
			} else {
				commentBytes = comment.getBytes();
			}
		}

		/**
		 * For Zip32 end information.
		 */
		public Builder withComment(String comment) {
			setComment(comment);
			return this;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public byte[] getExtensibleData() {
			return extensibleData;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public void setExtensibleData(byte[] extensibleData) {
			this.extensibleData = extensibleData;
		}

		/**
		 * Field only for Zip64 formats.
		 */
		public Builder withExtensibleData(byte[] extensibleData) {
			this.extensibleData = extensibleData;
			return this;
		}

		/**
		 * For zip64 end locator information.
		 */
		public int getNumberDisks() {
			return numberDisks;
		}

		/**
		 * For zip64 end locator information.
		 */
		public void setNumberDisks(int numberDisks) {
			this.numberDisks = numberDisks;
		}

		/**
		 * For zip64 end locator information.
		 */
		public Builder withNumberDisks(int numberDisks) {
			this.numberDisks = numberDisks;
			return this;
		}
	}
}
