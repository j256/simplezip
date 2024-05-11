package com.j256.simplezip.format.extra;

import java.io.IOException;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Central directory extended timestamp information.
 * 
 * @author graywatson
 */
public class ExtendedTimestampCentralExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5455;
	public static final int EXPECTED_MINIMUM_SIZE = 2 + 2 + 1;

	private static final int TIME_MODIFIED_FLAG = (1 << 0);
	private static final int TIME_ACCESSED_FLAG = (1 << 1);
	private static final int TIME_CREATED_FLAG = (1 << 2);

	private final int flags;
	private final long time;

	public ExtendedTimestampCentralExtraField(int size, int flags, long time) {
		super(EXPECTED_ID, size);
		this.flags = flags;
		this.time = time;
	}

	public int getFlags() {
		return flags;
	}

	public long getTime() {
		return time;
	}

	public boolean isTimeModified() {
		return ((flags & TIME_MODIFIED_FLAG) != 0);
	}

	public boolean isTimeAccessed() {
		return ((flags & TIME_ACCESSED_FLAG) != 0);
	}

	public boolean isTimeCreated() {
		return ((flags & TIME_CREATED_FLAG) != 0);
	}

	/**
	 * Read in the rest of the Zip64ExtraField after the id is read.
	 */
	public static ExtendedTimestampCentralExtraField read(RewindableInputStream input, int id, int size)
			throws IOException {
		Builder builder = new ExtendedTimestampCentralExtraField.Builder();
		builder.flags = IoUtils.readByte(input, "ExtendedTimestampCentralExtraField.flags");
		if (size >= EXPECTED_MINIMUM_SIZE + 8) {
			builder.time = IoUtils.readLong(input, "ExtendedTimestampCentralExtraField.time");
		}
		return builder.build();
	}

	/**
	 * Builder for the Zip64Info.
	 */
	public static class Builder {

		private int size;
		private int flags;
		private long time;;

		public ExtendedTimestampCentralExtraField build() {
			return new ExtendedTimestampCentralExtraField(size, flags, time);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public void setTimeModified(boolean timeModified) {
			if (timeModified) {
				this.flags |= TIME_MODIFIED_FLAG;
			} else {
				this.flags &= ~TIME_MODIFIED_FLAG;
			}
		}

		public void setTimeAccessed(boolean timeAccessed) {
			if (timeAccessed) {
				this.flags |= TIME_ACCESSED_FLAG;
			} else {
				this.flags &= ~TIME_ACCESSED_FLAG;
			}
		}

		public void setTimeCreated(boolean timeCreated) {
			if (timeCreated) {
				this.flags |= TIME_CREATED_FLAG;
			} else {
				this.flags &= ~TIME_CREATED_FLAG;
			}
		}
	}
}
