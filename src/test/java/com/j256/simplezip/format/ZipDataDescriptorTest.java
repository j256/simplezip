package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.ZipDataDescriptor.Builder;

public class ZipDataDescriptorTest {

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

	@Test
	public void testSpecialRead() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		IoUtils.writeInt(baos, ZipDataDescriptor.OPTIONAL_EXPECTED_SIGNATURE);
		int crc32 = 13123123;
		IoUtils.writeInt(baos, crc32);
		int compressedSize = 1789123;
		IoUtils.writeInt(baos, compressedSize);
		int uncompressedSize = 1723;
		IoUtils.writeInt(baos, uncompressedSize);

		ZipDataDescriptor dataDesc =
				ZipDataDescriptor.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024));

		assertEquals(crc32, dataDesc.getCrc32());
		assertEquals(compressedSize, dataDesc.getCompressedSize());
		assertEquals(uncompressedSize, dataDesc.getUncompressedSize());

		baos.reset();

		// now write without magic
		IoUtils.writeInt(baos, crc32);
		IoUtils.writeInt(baos, compressedSize);
		IoUtils.writeInt(baos, uncompressedSize);

		dataDesc =
				ZipDataDescriptor.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024));

		assertEquals(crc32, dataDesc.getCrc32());
		assertEquals(compressedSize, dataDesc.getCompressedSize());
		assertEquals(uncompressedSize, dataDesc.getUncompressedSize());
	}
}
