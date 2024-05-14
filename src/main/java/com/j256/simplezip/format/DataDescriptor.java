package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.CountingInfo;
import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Optional data descriptor immediately after the file-data. Only written if {@link GeneralPurposeFlag#DATA_DESCRIPTOR}
 * is set in the {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public class DataDescriptor {

	/** optional signature at the start of the data-descriptor */
	private static final int OPTIONAL_EXPECTED_SIGNATURE = 0x8074b50;

	private final long crc32;
	private final int compressedSize;
	private final int uncompressedSize;

	public DataDescriptor(long crc32, int compressedSize, int uncompressedSize) {
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
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
	public static DataDescriptor read(RewindableInputStream inputStream, CountingInfo countingInfo) throws IOException {
		Builder builder = new DataDescriptor.Builder();
		/*
		 * This is a little strange since there is an optional magic value according to Wikipedia. If the first value
		 * doesn't match the expected then we assume it is the CRC. If it does match the expected value then check the
		 * expected CRC to see if (by some coincidence) it matches the expected signature. If it does then we read the
		 * next 4 bytes to see if that is also the same CRC value if not then we sort of throw up our hands and assume
		 * that the first 4 bytes is the CRC without a signature and pray.
		 */
		int first = IoUtils.readInt(inputStream, "DataDescriptor.signature-or-crc32");
		if (first == OPTIONAL_EXPECTED_SIGNATURE) {
			builder.crc32 = IoUtils.readInt(inputStream, "DataDescriptor.crc32");
		} else {
			// guess that we have crc, compressed-size, uncompressed-size with the crc matching the signature
			builder.crc32 = first;
		}

		builder.compressedSize = IoUtils.readInt(inputStream, "DataDescriptor.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(inputStream, "DataDescriptor.uncompressedSize");

		// XXX: if the sizes are -1 then is there an additional 8+8 byte long sizes?

		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, OPTIONAL_EXPECTED_SIGNATURE);
		IoUtils.writeInt(outputStream, crc32);
		IoUtils.writeInt(outputStream, compressedSize);
		IoUtils.writeInt(outputStream, uncompressedSize);
	}

	public long getCrc32() {
		return crc32;
	}

	public int getCompressedSize() {
		return compressedSize;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	/**
	 * Builder for the DataDescriptor class.
	 */
	public static class Builder {
		private long crc32;
		private int compressedSize;
		private int uncompressedSize;

		/**
		 * Create a builder from an existing entry.
		 */
		public static Builder fromDescriptor(DataDescriptor dataDescriptor) {
			Builder builder = new Builder();
			builder.crc32 = dataDescriptor.crc32;
			builder.compressedSize = dataDescriptor.compressedSize;
			builder.uncompressedSize = dataDescriptor.uncompressedSize;
			return builder;
		}

		/**
		 * Reset the builder in case you want to reuse.
		 */
		public void reset() {
			crc32 = 0;
			compressedSize = 0;
			uncompressedSize = 0;
		}

		public DataDescriptor build() {
			return new DataDescriptor(crc32, compressedSize, uncompressedSize);
		}

		public long getCrc32() {
			return crc32;
		}

		public void setCrc32(long crc32) {
			this.crc32 = crc32;
		}

		public int getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(int compressedSize) {
			this.compressedSize = compressedSize;
		}

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(int uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}
	}
}
