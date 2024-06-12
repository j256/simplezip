package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Unit extra field #1. This has been superseded by the {@link ExtendedTimestampLocalExtraField} or newer versions of
 * the Unix extra fields such as {@link Unix2ExtraField}.
 * 
 * @author graywatson
 */
public class Unix1ExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5855;
	public static final int EXPECTED_MINIMUM_SIZE = 8 + 8;

	private final long timeLastAccessed;
	private final long timeLastModified;;
	private final Integer userId;
	private final Integer groupId;

	public Unix1ExtraField(int extraSize, long timeLastAccessed, long timeLastModified, Integer userId,
			Integer groupId) {
		super(EXPECTED_ID, extraSize);
		this.timeLastAccessed = timeLastAccessed;
		this.timeLastModified = timeLastModified;
		this.userId = userId;
		this.groupId = groupId;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read from the input-stream.
	 */
	public static Unix1ExtraField read(InputStream inputStream, int id, int size) throws IOException {
		byte[] tmpBytes = new byte[8]; 
		Builder builder = new Unix1ExtraField.Builder();
		builder.timeLastAccessed = IoUtils.readLong(inputStream, tmpBytes, "Unix1ExtraField.timeLastAccessed");
		builder.timeLastModified = IoUtils.readLong(inputStream, tmpBytes, "Unix1ExtraField.timeLastModified");
		if (size > EXPECTED_MINIMUM_SIZE) {
			builder.userId = IoUtils.readShort(inputStream, tmpBytes, "Unix1ExtraField.userId");
			builder.groupId = IoUtils.readShort(inputStream, tmpBytes, "Unix1ExtraField.groupId");
		}
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		byte[] tmpBytes = new byte[8]; 
		super.write(outputStream, tmpBytes);
		IoUtils.writeLong(outputStream, tmpBytes, timeLastAccessed);
		IoUtils.writeLong(outputStream, tmpBytes, timeLastModified);
		if (userId != null && groupId != null) {
			IoUtils.writeShort(outputStream, tmpBytes, userId);
			IoUtils.writeShort(outputStream, tmpBytes, groupId);
		}
	}

	public long getTimeLastAccessed() {
		return timeLastAccessed;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	/**
	 * Optional user-id.
	 * 
	 * @return user-id or null if not set.
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * Optional group-id.
	 * 
	 * @return group-id or null if not set.
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * Builder for the Unix1ExtraField.
	 */
	public static class Builder {
		private long timeLastAccessed;
		private long timeLastModified;;
		private Integer userId;
		private Integer groupId;

		public Unix1ExtraField build() {
			int size = EXPECTED_MINIMUM_SIZE;
			if (userId != null && groupId != null) {
				size += 2 + 2;
			}
			return new Unix1ExtraField(size, timeLastAccessed, timeLastModified, userId, groupId);
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
