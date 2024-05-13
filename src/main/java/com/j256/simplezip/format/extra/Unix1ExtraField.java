package com.j256.simplezip.format.extra;

import java.io.IOException;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Unit extra field #1. This has been superseded by the {@link ExtendedTimestampLocalExtraField} or newer versions of
 * the Unix extra fields such as {@link Unix2ExtraField}.
 * 
 * @author graywatson
 */
public class Unix1ExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5855;
	public static final int EXPECTED_MINIMUM_SIZE = 2 + 2 + 8 + 8;

	private final long timeLastAccess;
	private final long timeLastModified;;
	private final Integer userId;
	private final Integer groupId;

	public Unix1ExtraField(int extraSize, long timeLastAccess, long timeLastModified, Integer userId, Integer groupId) {
		super(EXPECTED_ID, extraSize);
		this.timeLastAccess = timeLastAccess;
		this.timeLastModified = timeLastModified;
		this.userId = userId;
		this.groupId = groupId;
	}

	public long getTimeLastAccess() {
		return timeLastAccess;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	/**
	 * Optional user-id. null if not set.
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * Optional group-id. null if not set.
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * Read in the rest of the Zip64ExtraField after the id is read.
	 */
	public static Unix1ExtraField read(RewindableInputStream input, int id, int size) throws IOException {
		Builder builder = new Unix1ExtraField.Builder();
		builder.size = size;
		builder.timeLastAccessed = IoUtils.readLong(input, "Unix1ExtraField.timeLastAccessed");
		builder.timeLastModified = IoUtils.readLong(input, "Unix1ExtraField.timeLastModified");
		if (size > EXPECTED_MINIMUM_SIZE) {
			builder.userId = IoUtils.readShort(input, "Unix1ExtraField.userId");
			builder.groupId = IoUtils.readShort(input, "Unix1ExtraField.groupId");
		}
		return builder.build();
	}

	/**
	 * Builder for the Unix1ExtraField.
	 */
	public static class Builder {
		private int size;
		private long timeLastAccessed;
		private long timeLastModified;;
		private Integer userId;
		private Integer groupId;

		public Unix1ExtraField build() {
			return new Unix1ExtraField(size, timeLastAccessed, timeLastModified, userId, groupId);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public long getTimeLastAccessed() {
			return timeLastAccessed;
		}

		public void setTimeLastAccessed(long timeLastAccess) {
			this.timeLastAccessed = timeLastAccess;
		}

		public long getTimeLastModified() {
			return timeLastModified;
		}

		public void setTimeLastModified(long timeLastModified) {
			this.timeLastModified = timeLastModified;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public Integer getGroupId() {
			return groupId;
		}

		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
	}
}
