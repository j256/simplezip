package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.DataDescriptor;
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

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ZipFileReader zipFile = new ZipFileReader(bais);

		ZipFileHeader header = zipFile.readFileHeader();
		assertEquals(fileName, header.getFileName());
		// there is no sizes because there is data-descriptor because we were streaming data in
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertEquals(0, header.getCrc32());
		assertTrue(header.getGeneralPurposeFlags().contains(GeneralPurposeFlag.DATA_DESCRIPTOR));
		assertEquals(CompressionMethod.DEFLATED, header.getCompressionMethod());

		byte[] buffer = new byte[10240];
		int numRead = zipFile.readFileData(buffer);

		assertEquals(fileBytes.length, numRead);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		// have to do this
		assertEquals(-1, zipFile.readFileData(buffer));

		DataDescriptor dataDescriptor = zipFile.getCurrentDataDescriptor();
		assertNotNull(dataDescriptor);
		assertEquals(fileBytes.length, dataDescriptor.getUncompressedSize());
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		assertEquals(crc32.getValue(), dataDescriptor.getCrc32());

		assertNull(zipFile.readFileHeader());

		CentralDirectoryFileHeader dirHeader = zipFile.readDirectoryFileHeader();
		assertNotNull(dirHeader);

		assertNull(zipFile.readDirectoryFileHeader());
		CentralDirectoryEnd end = zipFile.readDirectoryEnd();
		assertNotNull(end);
		zipFile.close();

		/*
		 * Write out our zip-file.
		 */

		baos.reset();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.writeFileHeader(header);
		writer.writeFileData(fileBytes);
		writer.finishFileData();
		writer.writeDataDescriptor(dataDescriptor);
		writer.writeDirectoryFileHeader(dirHeader);
		writer.writeDirectoryEnd(end);
		writer.close();

		bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertNull(zis.getNextEntry());
		zis.close();
	}
}
