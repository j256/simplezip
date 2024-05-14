package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

public class ZipFileWriterTest {

	@Test
	public void testStuff() throws IOException {

		String fileName = "hello";
		byte[] fileBytes = new byte[] { 1, 2, 3 };

		/*
		 * Write out our zip-file.
		 */

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);

		ZipFileHeader.Builder fileBuilder = ZipFileHeader.builder();
		fileBuilder.setGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.DATA_DESCRIPTOR);
		fileBuilder.setCompressionMethod(CompressionMethod.DEFLATED);
		fileBuilder.setLastModifiedDateTime(LocalDateTime.now());
		fileBuilder.setFileName(fileName);
		writer.writeFileHeader(fileBuilder.build());
		writer.writeFileDataPart(fileBytes);
		writer.finishFileData();
		writer.finishZip();
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(fileName, zipEntry.getName());
		byte[] buffer = new byte[1024];
		int numRead = zis.read(buffer);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		assertNull(zis.getNextEntry());
		zis.close();
	}
}
