package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;

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
		ZipFileHeader header = ZipFileHeader.read(new RewindableInputStream(input, 8192));
		assertEquals(entry.getName(), header.getFileName());
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertTrue(header.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR));
	}
}
