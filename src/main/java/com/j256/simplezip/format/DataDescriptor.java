package com.j256.simplezip.format;

import java.io.IOException;

import com.j256.simplezip.CountingInfo;
import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Data descriptor immediately after the {@link FileData}. Only written if {@link GeneralPurposeFlag#DATA_DESCRIPTOR} is
 * set in the {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public class DataDescriptor {

	/** optional signature at the start of the data-descriptor */
	private static final int OPTIONAL_EXPECTED_SIGNATURE = 0x8074b50;

	private final int signature;
	private final int crc32;
	private final int compressedSize;
	private final int uncompressedSize;

	public DataDescriptor(int signature, int crc32, int compressedSize, int uncompressedSize) {
		this.signature = signature;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
	}

	public static DataDescriptor read(RewindableInputStream input, CountingInfo countingInfo) throws IOException {
		/*
		 * This is a little strange since there is an optional magic value according to Wikipedia. If the first value
		 * doesn't match the expected then we assume it is the CRC. If it does match the expected value then check the
		 * expected CRC to see if (by some coincidence) it matches the expected signature. If it does then we read the
		 * next 4 bytes to see if that is also the same CRC value if not then we sort of throw up our hands and assume
		 * that the first 4 bytes is the CRC without a signature and pray.
		 */
		int first = IoUtils.readInt(input, "DataDescriptor.signature");
		if (first == OPTIONAL_EXPECTED_SIGNATURE) {

		}

		Builder builder = new DataDescriptor.Builder();
		builder.setCrc32(IoUtils.readInt(input, "DataDescriptor.crc32"));
		builder.setCompressedSize(IoUtils.readInt(input, "DataDescriptor.compressedSize"));
		builder.setUncompressedSize(IoUtils.readInt(input, "DataDescriptor.uncompressedSize"));
		return builder.build();
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the optional signature which should either match the expected value or be 0 if it didm't exist.
	 */
	public int getSignature() {
		return signature;
	}

	public int getCrc32() {
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
		private int signature;
		private int crc32;
		private int compressedSize;
		private int uncompressedSize;

		public DataDescriptor build() {
			return new DataDescriptor(signature, crc32, compressedSize, uncompressedSize);
		}

		public int getSignature() {
			return signature;
		}

		public void setSignature(int signature) {
			this.signature = signature;
		}

		public int getCrc32() {
			return crc32;
		}

		public void setCrc32(int crc32) {
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
