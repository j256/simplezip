package com.j256.simplezip.format.extra;

import java.io.IOException;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Zip64 extra-field information.
 * 
 * @author graywatson
 */
public class UnknownExtraField extends BaseExtraField {

	private final byte[] bytes;

	public UnknownExtraField(int id, int extraSize, byte[] bytes) {
		super(id, extraSize);
		this.bytes = bytes;
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
	public static UnknownExtraField read(RewindableInputStream input, int id, int extraSize) throws IOException {
		Builder builder = new UnknownExtraField.Builder();
		builder.id = id;
		builder.extraSize = extraSize;
		builder.bytes = IoUtils.readBytes(input, extraSize, "UnknownExtraField.bytes");
		return builder.build();
	}

	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Builder for the UnknownExtraField.
	 */
	public static class Builder {
		private int id;
		private int extraSize;
		private byte[] bytes;

		public UnknownExtraField build() {
			return new UnknownExtraField(id, extraSize, bytes);
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getExtraSize() {
			return extraSize;
		}

		public void setExtraSize(int extraSize) {
			this.extraSize = extraSize;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}
	}
}
