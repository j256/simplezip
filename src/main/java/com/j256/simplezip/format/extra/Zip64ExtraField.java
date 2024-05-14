package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Zip64 extra-field information.
 * 
 * @author graywatson
 */
public class Zip64ExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x0001;
	public static final int EXPECTED_SIZE = 2 + 2 + 8 + 8 + 8 + 4;

	private final long uncompressedSize;
	private final long compressedSize;
	private final long offset;
	private final int diskNumber;

	public Zip64ExtraField(long uncompressedSize, long compressedSize, long offset, int diskNumber) {
		super(EXPECTED_ID, EXPECTED_SIZE);
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.offset = offset;
		this.diskNumber = diskNumber;
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
	public static Zip64ExtraField read(RewindableInputStream inputStream, int id, int size) throws IOException {
		Builder builder = new Zip64ExtraField.Builder();
		builder.uncompressedSize = IoUtils.readLong(inputStream, "Zip64ExtraField.uncompressedSize");
		builder.compressedSize = IoUtils.readLong(inputStream, "Zip64ExtraField.compressedSize");
		builder.offset = IoUtils.readLong(inputStream, "Zip64ExtraField.offset");
		builder.diskNumber = IoUtils.readInt(inputStream, "Zip64ExtraField.diskNumber");
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeLong(outputStream, uncompressedSize);
		IoUtils.writeLong(outputStream, compressedSize);
		IoUtils.writeLong(outputStream, offset);
		IoUtils.writeInt(outputStream, diskNumber);
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	public long getCompressedSize() {
		return compressedSize;
	}

	public long getOffset() {
		return offset;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	/**
	 * Builder for the Zip64Info.
	 */
	public static class Builder {
		private long uncompressedSize;
		private long compressedSize;
		private long offset;
		private int diskNumber;

		public Zip64ExtraField build() {
			return new Zip64ExtraField(uncompressedSize, compressedSize, offset, diskNumber);
		}

		public long getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}

		public long getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}
	}
}
