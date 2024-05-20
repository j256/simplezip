package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.Zip64ExtraField.Builder;

public class Zip64ExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = Zip64ExtraField.builder();

		int compressedSize = 345832432;
		builder.setCompressedSize(compressedSize);
		assertEquals(compressedSize, builder.getCompressedSize());
		int diskNumber = 99553453;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		long offset = 44222343L;
		builder.setOffset(offset);
		assertEquals(offset, (int) builder.getOffset());
		int uncompressedSize = 213;
		builder.setUncompressedSize(uncompressedSize);
		assertEquals(uncompressedSize, builder.getUncompressedSize());

		Zip64ExtraField field = builder.build();
		assertEquals(compressedSize, field.getCompressedSize());
		assertEquals(diskNumber, field.getDiskNumber());
		assertEquals(offset, field.getOffset());
		assertEquals(uncompressedSize, field.getUncompressedSize());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = Zip64ExtraField.builder();

		int compressedSize = 345832432;
		builder.setCompressedSize(compressedSize);
		int diskNumber = 99553453;
		builder.setDiskNumber(diskNumber);
		long offset = 44222343L;
		builder.setOffset(offset);
		int uncompressedSize = 213;
		builder.setUncompressedSize(uncompressedSize);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), true);
		assertTrue(field instanceof Zip64ExtraField);
		Zip64ExtraField castField = (Zip64ExtraField) field;

		assertEquals(compressedSize, castField.getCompressedSize());
		assertEquals(diskNumber, castField.getDiskNumber());
		assertEquals(offset, castField.getOffset());
		assertEquals(uncompressedSize, castField.getUncompressedSize());
	}
}
