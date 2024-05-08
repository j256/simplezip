package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class LocalFileHeaderTest {

	@Test
	public void testStuff() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipEntry entry = new ZipEntry("hello");
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(entry);
		byte[] bytes1 = new byte[] { 1, 2, 3 };
		zos.write(bytes1);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileHeader header = ZipFileHeader.read(input);
		assertEquals(entry.getName(), header.getFileName());
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertEquals(new HashSet<GeneralPurposeFlag>(Arrays.asList(GeneralPurposeFlag.DATA_DESCRIPTOR)),
				header.getGeneralPurposeFlags());
	}
}
