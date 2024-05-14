package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;

public class ZipFileReaderTest {

	@Test
	public void testStuff() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[] { 1, 2, 3 };
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileReader zipFile = new ZipFileReader(input);

		ZipFileHeader header = zipFile.readFileHeader();
		assertEquals(fileName, header.getFileName());
		// there is no sizes because there is data-descriptor because we were streaming data in
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertEquals(0, header.getCrc32());
		assertTrue(header.getGeneralPurposeFlags().contains(GeneralPurposeFlag.DATA_DESCRIPTOR));
		assertEquals(CompressionMethod.DEFLATED, header.getCompressionMethod());

		System.out.println("header " + header.getFileName() + ", date " + header.getLastModFileDateTime() + ", time "
				+ header.getLastModifiedFileTimeString() + ", size " + header.getUncompressedSize() + ", method "
				+ header.getCompressionMethod() + ", extra " + Arrays.toString(header.getExtraFieldBytes()));

		baos.reset();
		long numRead = zipFile.readFileData(baos);
		assertEquals(bytes.length, numRead);
		assertArrayEquals(bytes, baos.toByteArray());

		DataDescriptor dataDescriptor = zipFile.getCurrentDataDescriptor();
		assertNotNull(dataDescriptor);
		assertEquals(bytes.length, dataDescriptor.getUncompressedSize());
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		assertEquals(crc32.getValue(), dataDescriptor.getCrc32());

		assertNull(zipFile.readFileHeader());

		CentralDirectoryFileHeader dirHeader = zipFile.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		System.out.println("dir " + dirHeader.getFileName() + ", size " + dirHeader.getUncompressedSize() + ", method "
				+ dirHeader.getCompressionMethod() + ", extra " + Arrays.toString(dirHeader.getExtraFieldBytes()));

		assertNull(zipFile.readDirectoryFileHeader());
		CentralDirectoryEnd end = zipFile.readDirectoryEnd();
		assertNotNull(end);
		System.out.println("end: num-records " + end.getNumRecordsTotal() + ", size " + end.getDirectorySize());

		zipFile.close();
	}
}
