package com.j256.simplezip;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

public class ZipFileWriterTest {

	@Test
	public void testStuff() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		byte[] fileBytes = new byte[] { 1, 2, 3 };
		zos.write(fileBytes);
		zos.closeEntry();
		zos.close();

		/*
		 * Write out our zip-file.
		 */

		ZipFileWriter writer = new ZipFileWriter(baos);

		ZipFileHeader.Builder fileBuilder = ZipFileHeader.builder();
		fileBuilder.setGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.DATA_DESCRIPTOR);
		fileBuilder.setLastModifiedDateTime(LocalDateTime.now());
		fileBuilder.setFileName("hello");
		writer.writeFileHeader(fileBuilder.build());
		writer.writeFileData(fileBytes);
		writer.finishFileData();
		CentralDirectoryFileHeader.Builder dirBuilder = CentralDirectoryFileHeader.builder();
		writer.writeDirectoryFileHeader(dirBuilder.build());
		writer.writeDirectoryEnd();
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertNull(zis.getNextEntry());
		zis.close();
	}
}
