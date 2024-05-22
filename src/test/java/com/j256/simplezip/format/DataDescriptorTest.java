package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.simplezip.format.ZipDataDescriptor.Builder;

public class DataDescriptorTest {

	@Test
	public void testCoverage() {
		Builder builder = ZipDataDescriptor.builder();

		int crc32 = 1312;
		builder.setCrc32(crc32);
		assertEquals(crc32, builder.getCrc32());
		int compressedSize = 5251312;
		builder.setCompressedSize(compressedSize);
		assertEquals(compressedSize, builder.getCompressedSize());
		int uncompressedSize = 565479567;
		builder.setUncompressedSize(uncompressedSize);
		assertEquals(uncompressedSize, builder.getUncompressedSize());

		ZipDataDescriptor dataDesc = builder.build();
		assertEquals(crc32, dataDesc.getCrc32());
		assertEquals(compressedSize, dataDesc.getCompressedSize());
		assertEquals(uncompressedSize, dataDesc.getUncompressedSize());

		builder = ZipDataDescriptor.Builder.fromDescriptor(dataDesc);
		assertEquals(crc32, builder.getCrc32());
		assertEquals(compressedSize, builder.getCompressedSize());
		assertEquals(uncompressedSize, builder.getUncompressedSize());
	}
}
