package com.j256.simplezip.format;

/**
 * Additional information that can be written at the very end of the zip file in a {@link ZipCentralDirectoryEnd}
 * structure.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryEndInfo {

	private final int diskNumber;
	private final int diskNumberStart;
	private final byte[] commentBytes;

	public ZipCentralDirectoryEndInfo(int diskNumber, int diskNumberStart, byte[] commentBytes) {
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.commentBytes = commentBytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
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
	 * Builder for the {@link ZipCentralDirectoryEndInfo}.
	 */
	public static class Builder {
		private int diskNumber = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private byte[] commentBytes;

		/**
		 * Create a builder from an existing central-directory file-entry.
		 */
		public static Builder fromCentralDirectoryEnd(ZipCentralDirectoryEnd end) {
			Builder builder = new Builder();
			builder.diskNumber = end.getDiskNumber();
			builder.diskNumberStart = end.getDiskNumberStart();
			builder.commentBytes = end.getCommentBytes();
			return builder;
		}

		/**
		 * Builder an instance of the central-directory file-header.
		 */
		public ZipCentralDirectoryEndInfo build() {
			return new ZipCentralDirectoryEndInfo(diskNumber, diskNumberStart, commentBytes);
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

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}

		public Builder withCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
			return this;
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

		public Builder withComment(String comment) {
			setComment(comment);
			return this;
		}
	}
}
