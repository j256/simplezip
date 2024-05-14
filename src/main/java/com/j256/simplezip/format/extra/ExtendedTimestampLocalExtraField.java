package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Local file header extended timestamp information with last-modified, accessed, and created time stamps.
 * 
 * @author graywatson
 */
public class ExtendedTimestampLocalExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5455;
	public static final int EXPECTED_SIZE = 2 + 2 + 1 + 8 + 8 + 8;

	private final int flags;
	private final long timeLastModified;;
	private final long timeLastAccessed;
	private final long timeCreated;

	public ExtendedTimestampLocalExtraField(int flags, long timeLastModified, long timeLastAccessed, long timeCreated) {
		super(EXPECTED_ID, EXPECTED_SIZE);
		this.flags = flags;
		this.timeLastModified = timeLastModified;
		this.timeLastAccessed = timeLastAccessed;
		this.timeCreated = timeCreated;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read in the rest of the Zip64ExtraField after the id is read.
	 */
	public static ExtendedTimestampLocalExtraField read(RewindableInputStream inputStream, int id, int size)
			throws IOException {
		Builder builder = new ExtendedTimestampLocalExtraField.Builder();
		builder.flags = IoUtils.readByte(inputStream, "ExtendedTimestampLocalExtraField.flags");
		builder.timeLastModified = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeLastModified");
		builder.timeLastAccessed = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeLastAccessed");
		builder.timeCreated = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeCreated");
		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	@Override
	public void write(OutputStream inputStream) throws IOException {
		super.write(inputStream);
		IoUtils.writeByte(inputStream, flags);
		IoUtils.writeLong(inputStream, timeLastAccessed);
		IoUtils.writeLong(inputStream, timeLastModified);
		IoUtils.writeLong(inputStream, timeCreated);
	}

	public int getFlags() {
		return flags;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public long getTimeLastAccessed() {
		return timeLastAccessed;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * Builder for the Zip64Info.
	 */
	public static class Builder {

		private int flags;
		private long timeLastModified;;
		private long timeLastAccessed;
		private long timeCreated;

		public ExtendedTimestampLocalExtraField build() {
			return new ExtendedTimestampLocalExtraField(flags, timeLastModified, timeLastAccessed, timeCreated);
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}

		public long getTimeLastModified() {
			return timeLastModified;
		}

		public void setTimeLastModified(long timeLastModified) {
			this.timeLastModified = timeLastModified;
		}

		public long getTimeLastAccessed() {
			return timeLastAccessed;
		}

		public void setTimeLastAccessed(long timeLastAccess) {
			this.timeLastAccessed = timeLastAccess;
		}

		public long getTimeCreated() {
			return timeCreated;
		}

		public void setTimeCreated(long timeCreation) {
			this.timeCreated = timeCreation;
		}
	}
}
