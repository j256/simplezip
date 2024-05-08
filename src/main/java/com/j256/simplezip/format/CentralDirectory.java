package com.j256.simplezip.format;

import java.io.IOException;

import com.j256.simplezip.CountingInfo;
import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.DataDescriptor.Builder;

public class CentralDirectory {

	/**
	 * signature that is expected to be at the front of the central directory. Since the file-headers come first we use
	 * the expected header from them.
	 */
	public static final int EXPECTED_SIGNATURE = CentralDirectoryFileHeader.EXPECTED_SIGNATURE;

	private CentralDirectoryFileHeader[] fileHeaders;

	public static DataDescriptor read(RewindableInputStream input, CountingInfo countingInfo) throws IOException {
		/*
		 * This is a little strange since there is an optional magic value according to Wikipedia. If the first value
		 * doesn't match the expected then we assume it is the CRC. If it does match the expected value then check the
		 * expected CRC to see if (by some coincidence) it matches the expected signature. If it does then we read the
		 * next 4 bytes to see if that is also the same CRC value if not then we sort of throw up our hands and assume
		 * that the first 4 bytes is the CRC without a signature and pray.
		 */
		int first = IoUtils.readInt(input, "DataDescriptor.signature");
		if (first == EXPECTED_SIGNATURE) {

		}

		Builder builder = new DataDescriptor.Builder();
		builder.setCrc32(IoUtils.readInt(input, "DataDescriptor.crc32"));
		builder.setCompressedSize(IoUtils.readInt(input, "DataDescriptor.compressedSize"));
		builder.setUncompressedSize(IoUtils.readInt(input, "DataDescriptor.uncompressedSize"));
		return builder.build();
	}
}
