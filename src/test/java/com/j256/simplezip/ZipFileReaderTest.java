package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
		ZipFileReader reader = new ZipFileReader(input);

		assertNull(reader.getCurrentFileNameAsString());
		ZipFileHeader header = reader.readFileHeader();
		assertEquals(reader.getCurrentFileNameAsString(), header.getFileName());
		assertEquals(fileName, header.getFileName());
		// there is no sizes because there is data-descriptor because we were streaming data in
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertEquals(0, header.getCrc32());
		assertTrue(header.getGeneralPurposeFlagAsEnums().contains(GeneralPurposeFlag.DATA_DESCRIPTOR));
		assertEquals(CompressionMethod.DEFLATED, header.getCompressionMethodAsEnum());

		System.out.println("header " + header.getFileName() + ", date " + header.getLastModFileDateTime() + ", time "
				+ header.getLastModifiedFileTimeString() + ", size " + header.getUncompressedSize() + ", method "
				+ header.getCompressionMethod() + ", extra " + Arrays.toString(header.getExtraFieldBytes()));

		baos.reset();
		// empty read
		assertEquals(0, reader.readFileData(new byte[0]));
		long numRead = reader.readFileData(baos);
		assertEquals(bytes.length, numRead);
		assertArrayEquals(bytes, baos.toByteArray());

		DataDescriptor dataDescriptor = reader.getCurrentDataDescriptor();
		assertNotNull(dataDescriptor);
		assertEquals(bytes.length, dataDescriptor.getUncompressedSize());
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		assertEquals(crc32.getValue(), dataDescriptor.getCrc32());

		assertNull(reader.readFileHeader());

		CentralDirectoryFileHeader dirHeader = reader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		System.out.println("dir " + dirHeader.getFileName() + ", size " + dirHeader.getUncompressedSize() + ", method "
				+ dirHeader.getCompressionMethod() + ", extra " + Arrays.toString(dirHeader.getExtraFieldBytes()));

		assertNull(reader.readDirectoryFileHeader());
		CentralDirectoryEnd end = reader.readDirectoryEnd();
		assertNotNull(end);
		System.out.println("end: num-records " + end.getNumRecordsTotal() + ", size " + end.getDirectorySize());

		reader.close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromBadFile() throws IOException {
		new ZipFileReader(new File("/doesnotexist")).close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromBadPath() throws IOException {
		new ZipFileReader("/doesnotexist").close();
	}

	@Test
	public void testReadFromNullFile() throws IOException {
		new ZipFileReader(new File("/dev/null")).close();
	}

	@Test
	public void testReadFromNullPath() throws IOException {
		new ZipFileReader("/dev/null").close();
	}

	@Test
	public void testReadBigBuffer() throws IOException {
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
		ZipFileReader reader = new ZipFileReader(input);
		assertNotNull(reader.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = reader.readFileData(buffer);
		assertEquals(bytes.length, num);
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));
		reader.close();
	}

	@Test(expected = IllegalStateException.class)
	public void readFileDataWithoutHeader() throws IOException {
		ZipFileReader reader = new ZipFileReader(new ByteArrayInputStream(new byte[0]));
		reader.readFileData(new byte[0]);
	}

	@Test
	public void readEmptyFileData() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "empty";
		ZipEntry zipEntry = new ZipEntry(fileName);
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[0];
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileReader reader = new ZipFileReader(input);
		assertNotNull(reader.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = reader.readFileData(buffer);
		assertEquals(-1, num);
		reader.close();
	}

	@Test
	public void testReadSlowly() throws IOException {
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
		ZipFileReader reader = new ZipFileReader(input);
		assertNotNull(reader.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = reader.readFileData(buffer, 0, 1);
		assertEquals(1, num);
		assertFalse(reader.isFileDataEofReached());
		num = reader.readFileData(buffer, 1, buffer.length - 1);
		assertTrue(reader.isFileDataEofReached());
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num + 1));
		reader.close();
	}

	@Test
	public void testReadStored() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipEntry.setMethod(ZipEntry.STORED);
		byte[] bytes = new byte[] { 1, 2, 3 };
		zipEntry.setSize(bytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		zipEntry.setCrc(crc32.getValue());
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileReader reader = new ZipFileReader(input);
		ZipFileHeader header = reader.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		byte[] buffer = new byte[1024];
		int num = reader.readFileData(buffer, 0, buffer.length);
		assertEquals(bytes.length, num);
		assertEquals(-1, reader.readFileData(buffer, 0, buffer.length));
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));
		reader.close();
	}

	@Test
	public void testReadStoredEmpty() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipEntry.setMethod(ZipEntry.STORED);
		byte[] bytes = new byte[0];
		zipEntry.setSize(bytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		zipEntry.setCrc(crc32.getValue());
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileReader reader = new ZipFileReader(input);
		ZipFileHeader header = reader.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		byte[] buffer = new byte[1024];
		assertEquals(-1, reader.readFileData(buffer, 0, buffer.length));
		reader.close();
	}
}
