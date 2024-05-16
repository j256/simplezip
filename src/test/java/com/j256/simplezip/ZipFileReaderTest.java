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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.DataDescriptor;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;

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
		assertEquals(0, reader.readFileDataPart(new byte[0]));
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
		int num = reader.readFileDataPart(buffer);
		assertEquals(bytes.length, num);
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));
		reader.close();
	}

	@Test(expected = IllegalStateException.class)
	public void readFileDataWithoutHeader() throws IOException {
		ZipFileReader reader = new ZipFileReader(new ByteArrayInputStream(new byte[0]));
		reader.readFileDataPart(new byte[0]);
	}

	@Test(expected = IllegalStateException.class)
	public void readRawFileDataWithoutHeader() throws IOException {
		ZipFileReader reader = new ZipFileReader(new ByteArrayInputStream(new byte[0]));
		reader.readRawFileDataPart(new byte[0]);
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
		int num = reader.readFileDataPart(buffer);
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
		int num = reader.readFileDataPart(buffer, 0, 1);
		assertEquals(1, num);
		assertFalse(reader.isFileDataEofReached());
		num = reader.readFileDataPart(buffer, 1, buffer.length - 1);
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
		int num = reader.readFileDataPart(buffer, 0, buffer.length);
		assertEquals(bytes.length, num);
		assertEquals(-1, reader.readFileDataPart(buffer, 0, buffer.length));
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
		assertEquals(-1, reader.readFileDataPart(buffer, 0, buffer.length));
		reader.close();
	}

	@Test(expected = IOException.class)
	public void testWriteBadDeflateData() throws IOException {
		byte[] fileBytes = new byte[] { (byte) 200, (byte) 200, 2, 1 };
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.DEFLATED);
		String name = "hello.bin";
		builder.setFileName(name);
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32(crc32.getValue());
		writer.writeFileHeader(builder.build());
		writer.writeRawFileDataPart(fileBytes);
		System.out.println("wrote raw file, offset = " + writer.finishFileData());
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipFileReader reader = new ZipFileReader(bais);
		ZipFileHeader header = reader.readFileHeader();
		assertNotNull(header);
		assertEquals(name, header.getFileName());
		byte[] buffer = new byte[1024];
		reader.readFileDataPart(buffer);
		reader.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteUnknownMethod() throws IOException {
		byte[] fileBytes = new byte[] { (byte) 200, (byte) 200, 2, 1 };
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.IBM_TERSE);
		String name = "hello.bin";
		builder.setFileName(name);
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32(crc32.getValue());
		writer.writeFileHeader(builder.build());
		writer.writeRawFileDataPart(fileBytes);
		System.out.println("wrote raw file, offset = " + writer.finishFileData());
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipFileReader reader = new ZipFileReader(bais);
		ZipFileHeader header = reader.readFileHeader();
		assertNotNull(header);
		assertEquals(name, header.getFileName());
		byte[] buffer = new byte[1024];
		reader.readFileDataPart(buffer);
		reader.close();
	}

	@Test
	public void testReadRaw() throws IOException, DataFormatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		byte[] bytes = new byte[] { 1, 2, 3 };
		zipEntry.setSize(bytes.length);
		zipEntry.setCompressedSize(5);
		// deflated
		zipEntry.setMethod(ZipEntry.DEFLATED);
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		zipEntry.setCrc(crc32.getValue());
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		RewindableInputStream input = new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240);
		ZipFileReader reader = new ZipFileReader(input);
		assertNotNull(reader.readFileHeader());
		baos.reset();
		reader.readRawFileData(baos);

		// now we inflate the data externally
		Inflater inflater = new Inflater(true /* no wrap */);
		inflater.setInput(baos.toByteArray(), 0, baos.size());
		byte[] output = new byte[1024];
		int numInflated = inflater.inflate(output, 0, output.length);
		assertTrue(inflater.finished());
		inflater.end();
		assertArrayEquals(bytes, Arrays.copyOf(output, numInflated));

		reader.close();
	}

	@Test
	public void testReadToFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipEntry zipEntry = new ZipEntry(fileName);
		byte[] bytes = new byte[] { 1, 2, 3 };
		zipEntry.setSize(bytes.length);
		zipEntry.setCompressedSize(5);
		zipEntry.setMethod(ZipEntry.DEFLATED);
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		zipEntry.setCrc(crc32.getValue());
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(zipEntry);
		zos.write(bytes);
		zos.closeEntry();
		// do it twice
		zipEntry = new ZipEntry(fileName + "2");
		zipEntry.setSize(bytes.length);
		zipEntry.setCompressedSize(5);
		zipEntry.setMethod(ZipEntry.DEFLATED);
		zipEntry.setCrc(crc32.getValue());
		zos.putNextEntry(zipEntry);
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		RewindableInputStream input = new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240);
		ZipFileReader reader = new ZipFileReader(input);
		assertNotNull(reader.readFileHeader());
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		reader.readFileData(file);
		byte[] output = readFileToBytes(file);
		assertArrayEquals(bytes, output);
		file.delete();

		assertNotNull(reader.readFileHeader());
		file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		reader.readFileData(file.getPath());
		output = readFileToBytes(file);
		assertArrayEquals(bytes, output);
		file.delete();

		reader.close();
	}

	private byte[] readFileToBytes(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[1024];
			while (true) {
				int num = fis.read(buffer);
				if (num < 0) {
					break;
				}
				baos.write(buffer, 0, num);
			}
			return baos.toByteArray();
		}
	}
}
