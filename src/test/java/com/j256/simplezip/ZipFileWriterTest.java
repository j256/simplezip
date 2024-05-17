package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryEnd;
import com.j256.simplezip.format.CentralDirectoryFileHeader;
import com.j256.simplezip.format.CentralDirectoryFileInfo;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;

public class ZipFileWriterTest {

	@Test
	public void testStuff() throws IOException {

		String fileName1 = "hello";
		byte[] fileBytes1 = new byte[] { 1, 2, 3 };

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream testStream = getClass().getResourceAsStream("/src.tgz");) {
			assertNotNull(testStream);
			byte[] buffer = new byte[8192];
			while (true) {
				int num = testStream.read(buffer);
				if (num < 0) {
					break;
				}
				baos.write(buffer, 0, num);
			}
		}
		byte[] fileBytes2 = baos.toByteArray();

		/*
		 * Write out our zip-file.
		 */

		baos.reset();

		ZipFileWriter writer = new ZipFileWriter(baos);
		assertEquals(0, writer.getNumBytesWritten());

		ZipFileHeader.Builder fileHeaderBuilder = ZipFileHeader.builder();
		fileHeaderBuilder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL,
				GeneralPurposeFlag.DATA_DESCRIPTOR);
		fileHeaderBuilder.setCompressionMethod(CompressionMethod.DEFLATED);
		fileHeaderBuilder.setLastModifiedDateTime(LocalDateTime.now());
		fileHeaderBuilder.setFileName(fileName1);
		writer.writeFileHeader(fileHeaderBuilder.build());
		writer.writeFileDataPart(fileBytes1);
		writer.finishFileData();
		assertNotEquals(0, writer.getNumBytesWritten());

		// write another file
		fileHeaderBuilder.reset();
		fileHeaderBuilder.setCompressionMethod(CompressionMethod.NONE);
		fileHeaderBuilder.setLastModifiedDateTime(LocalDateTime.now());
		String fileName2 = "src.tgz";
		fileHeaderBuilder.setFileName(fileName2);
		fileHeaderBuilder.setCompressedSize(fileBytes2.length);
		fileHeaderBuilder.setUncompressedSize(fileBytes2.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes2);
		fileHeaderBuilder.setCrc32(crc32.getValue());
		writer.writeFileHeader(fileHeaderBuilder.build());
		writer.writeFileDataPart(fileBytes2);
		writer.finishFileData();
		// double finish
		writer.finishZip();
		writer.finishZip();
		writer.flush();
		writer.close();
		System.out.println("wrote " + writer.getNumBytesWritten() + " bytes to zip output");

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = zis.getNextEntry();

		assertNotNull(zipEntry);
		assertEquals(fileName1, zipEntry.getName());
		assertEquals(ZipEntry.DEFLATED, zipEntry.getMethod());
		byte[] buffer = new byte[1024];
		int numRead = zis.read(buffer);
		assertArrayEquals(fileBytes1, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(fileName2, zipEntry.getName());
		assertEquals(ZipEntry.STORED, zipEntry.getMethod());

		baos.reset();
		while (true) {
			numRead = zis.read(buffer);
			if (numRead < 0) {
				break;
			}
			baos.write(buffer, 0, numRead);
		}
		assertArrayEquals(fileBytes2, baos.toByteArray());

		assertNull(zis.getNextEntry());
		zis.close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testWriteToBadFile() throws IOException {
		new ZipFileWriter(new File("/doesnotexist")).close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testWriteToBadPath() throws IOException {
		new ZipFileWriter("/doesnotexist").close();
	}

	@Test
	public void testWriteToNullFile() throws IOException {
		new ZipFileWriter(new File("/dev/null")).close();
	}

	@Test
	public void testWriteToNullPath() throws IOException {
		new ZipFileWriter("/dev/null").close();
	}

	@Test
	public void testReadEmptyZip() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ZipFileWriter(baos).close();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));) {
			assertNull(zis.getNextEntry());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.close();
		writer.writeFileHeader(ZipFileHeader.builder().build());
	}

	@Test(expected = IllegalStateException.class)
	public void testAddFileInfoAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.close();
		writer.addDirectoryFileInfo(CentralDirectoryFileInfo.builder().build());
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteFileDataAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.close();
		writer.writeFileDataPart(new byte[0]);
	}

	@Test(expected = IllegalStateException.class)
	public void testFinishFileDataAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.close();
		writer.finishFileData();
	}

	@Test(expected = IllegalStateException.class)
	public void testCloseWithoutFnishingFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.writeFileHeader(ZipFileHeader.builder().build());
		writer.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteNextHeaderWithoutFnishingFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.writeFileHeader(ZipFileHeader.builder().build());
		writer.writeFileHeader(ZipFileHeader.builder().build());
		writer.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteDataWithoutHeader() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.writeFileDataPart(new byte[0]);
		writer.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteRawDataWithoutHeader() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.writeRawFileDataPart(new byte[0]);
		writer.close();
	}

	@Test
	public void testWriteFilePath() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		byte[] fileBytes = new byte[] { 3, 4, 2, 1 };
		try (FileOutputStream fos = new FileOutputStream(file);) {
			fos.write(fileBytes);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setFileName(file.getName());
		writer.writeFileHeader(builder.build());
		writer.writeFile(file);
		writer.writeFileHeader(builder.build());
		writer.writeFile(file.getPath());
		writer.writeFileHeader(builder.build());
		writer.writeFileDataPart(fileBytes);
		writer.finishFileData();
		writer.close();
		file.delete();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(file.getName(), zipEntry.getName());
		assertEquals(ZipEntry.DEFLATED, zipEntry.getMethod());
		byte[] buffer = new byte[1024];
		int numRead = zis.read(buffer);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(file.getName(), zipEntry.getName());
		assertEquals(ZipEntry.DEFLATED, zipEntry.getMethod());
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(file.getName(), zipEntry.getName());
		assertEquals(ZipEntry.DEFLATED, zipEntry.getMethod());
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNull(zipEntry);
		zis.close();
	}

	@Test
	public void testWriteRawFilePath() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		byte[] fileBytes = new byte[] { 3, 4, 2, 1 };
		try (FileOutputStream fos = new FileOutputStream(file);) {
			fos.write(fileBytes);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName());
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32Value(crc32);
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file));
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file.getPath()));
		System.out.println("wrote end, offset = " + writer.finishZip());
		writer.close();
		file.delete();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(file.getName(), zipEntry.getName());
		assertEquals(ZipEntry.STORED, zipEntry.getMethod());
		byte[] buffer = new byte[1024];
		int numRead = zis.read(buffer);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNotNull(zipEntry);
		assertEquals(file.getName(), zipEntry.getName());
		assertEquals(ZipEntry.STORED, zipEntry.getMethod());
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, numRead));
		zipEntry = zis.getNextEntry();
		assertNull(zipEntry);
		zis.close();
	}

	@Test
	public void testWriteRawBufferredFilePath() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		byte[] fileBytes = new byte[] { 3, 4, 2, 1 };
		try (FileOutputStream fos = new FileOutputStream(file);) {
			fos.write(fileBytes);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.enableBufferedOutput(10240, 10240);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file));
		builder.setFileName(file.getName() + "2");
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file.getPath()));
		System.out.println("wrote end, offset = " + writer.finishZip());
		writer.close();
		file.delete();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// read using ZipInputStream
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry entry = zis.getNextEntry();
		assertEquals(file.getName() + "1", entry.getName());
		byte[] buffer = new byte[1024];
		int num = zis.read(buffer);
		assertEquals(fileBytes.length, num);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, num));
		entry = zis.getNextEntry();
		assertNotNull(entry);
		assertEquals(file.getName() + "2", entry.getName());
		num = zis.read(buffer);
		assertEquals(fileBytes.length, num);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, num));
		zis.close();
	}

	@Test
	public void testWriteRawShortBufferFilePath() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		byte[] fileBytes = new byte[] { 3, 4, 2, 1 };
		try (FileOutputStream fos = new FileOutputStream(file);) {
			fos.write(fileBytes);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.enableBufferedOutput(10240, 2);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file));
		builder.setFileName(file.getName() + "2");
		writer.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + writer.writeRawFile(file.getPath()));
		System.out.println("wrote end, offset = " + writer.finishZip());
		writer.flush();
		writer.close();
		file.delete();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// read using ZipInputStream
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry entry = zis.getNextEntry();
		assertEquals(file.getName() + "1", entry.getName());
		byte[] buffer = new byte[1024];
		int num = zis.read(buffer);
		assertEquals(fileBytes.length, num);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, num));
		entry = zis.getNextEntry();
		assertNotNull(entry);
		assertEquals(file.getName() + "2", entry.getName());
		num = zis.read(buffer);
		assertEquals(fileBytes.length, num);
		assertArrayEquals(fileBytes, Arrays.copyOf(buffer, num));
		zis.close();
	}

	@Test
	public void testAddFileInfo() throws IOException {
		byte[] fileBytes = new byte[] { 3, 4, 2, 1 };

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setFileName("hello");
		writer.writeFileHeader(builder.build());
		CentralDirectoryFileInfo.Builder fileInfoBuilder = CentralDirectoryFileInfo.builder();
		String comment = "hrm a nice lookin' file";
		fileInfoBuilder.setComment(comment);
		writer.addDirectoryFileInfo(fileInfoBuilder.build());
		writer.writeFileDataPart(fileBytes, 0, 1);
		writer.writeFileDataPart(fileBytes, 1, fileBytes.length - 1);
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileReader reader = new ZipFileReader(bais);
		assertNotNull(reader.readFileHeader());
		assertEquals(fileBytes.length, reader.skipFileData());
		assertNull(reader.readFileHeader());
		CentralDirectoryFileHeader dirHeader = reader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		reader.close();
	}

	@Test
	public void testEndComment() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		String comment = "the very end";
		writer.finishZip(comment);
		writer.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileReader reader = new ZipFileReader(bais);
		assertNull(reader.readFileHeader());
		assertNull(reader.readDirectoryFileHeader());
		CentralDirectoryEnd end = reader.readDirectoryEnd();
		assertEquals(comment, end.getComment());
		reader.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testUnknownMethod() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setFileName("hello");
		builder.setCompressionMethod(CompressionMethod.IBM_TERSE);
		writer.writeFileHeader(builder.build());
		writer.writeFileDataPart(new byte[0]);
		writer.close();
	}

	@Test
	public void testFileInAFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileWriter writer = new ZipFileWriter(baos);
		writer.enableBufferedOutput(1024 * 1024, 1024 * 1024);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		String outerFileName = "outer.bin";
		builder.setFileName(outerFileName);
		writer.writeFileHeader(builder.build());
		byte[] outerFileBytes = new byte[] { 1, 2, 3, 4, 5, 6 };
		writer.writeFileDataPart(outerFileBytes);
		writer.finishFileData();

		String zipName = "hello.zip";
		builder.setFileName(zipName);
		writer.writeFileHeader(builder.build());

		// write an inner zip file
		ZipFileWriter innerWriter = new ZipFileWriter(writer.openFileDataOutputStream(false));
		innerWriter.enableBufferedOutput(1024 * 1024, 1024 * 1024);
		String innerFileName = "inner.bin";
		builder.setFileName(innerFileName);
		innerWriter.writeFileHeader(builder.build());
		byte[] innerFileBytes = new byte[] { 10, 20, 30, 40, 50, 60 };
		innerWriter.writeFileDataPart(innerFileBytes);
		innerWriter.finishFileData();
		innerWriter.close();

		writer.close();

		/*
		 * Read it back in.
		 */

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// read using ZipInputStream
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry entry = zis.getNextEntry();
		assertEquals(outerFileName, entry.getName());
		byte[] buffer = new byte[1024];
		int num = zis.read(buffer);
		assertArrayEquals(outerFileBytes, Arrays.copyOf(buffer, num));

		entry = zis.getNextEntry();
		assertEquals(zipName, entry.getName());
		// process inner zip file
		ZipInputStream innerZis = new ZipInputStream(zis);
		entry = innerZis.getNextEntry();
		assertEquals(innerFileName, entry.getName());
		num = innerZis.read(buffer);
		assertArrayEquals(innerFileBytes, Arrays.copyOf(buffer, num));
		assertNull(innerZis.getNextEntry());
		innerZis.close();

		zis.close();

		/*
		 * now read it using our reader
		 */

		bais.reset();

		// read using ZipInputStream
		ZipFileReader reader = new ZipFileReader(bais);
		ZipFileHeader header = reader.readFileHeader();
		assertEquals(outerFileName, header.getFileName());
		num = reader.readFileDataPart(buffer);
		assertArrayEquals(outerFileBytes, Arrays.copyOf(buffer, num));
		assertEquals(-1, reader.readFileDataPart(buffer));

		header = reader.readFileHeader();
		assertNotNull(header);
		assertEquals(zipName, header.getFileName());
		// process inner zip file
		ZipFileReader innerReader = new ZipFileReader(reader.openFileDataOutputStream(false));
		header = innerReader.readFileHeader();
		assertEquals(innerFileName, header.getFileName());
		num = innerReader.readFileDataPart(buffer);
		assertArrayEquals(innerFileBytes, Arrays.copyOf(buffer, num));
		assertEquals(-1, innerReader.readFileDataPart(buffer));
		assertNull(innerReader.readFileHeader());
		CentralDirectoryFileHeader dirHeader = innerReader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(innerFileName, dirHeader.getFileName());
		assertNull(innerReader.readDirectoryFileHeader());

		// NOTE we need to make sure that we read all of the bytes from the inner file
		innerReader.close();
		innerReader = null;

		assertNull(reader.readFileHeader());
		dirHeader = reader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(outerFileName, dirHeader.getFileName());

		reader.close();
	}

	public static void main(String[] args) throws IOException {

		byte[] fileBytes =
				"let's see what zip does with this string to compress.  can it compress it more than 0%".getBytes();

		File tmpFile = new File("/tmp/x.zip");
		ZipFileWriter writer = new ZipFileWriter(tmpFile);
		writer.enableBufferedOutput(10240, 10240);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setLastModifiedDateTime(LocalDateTime.now());
		builder.setFileName("hello");
		writer.writeFileHeader(builder.build());
		CentralDirectoryFileInfo.Builder fileInfoBuilder = CentralDirectoryFileInfo.builder();
		String comment = "hrm a nice lookin' file";
		fileInfoBuilder.setComment(comment);
		fileInfoBuilder.setTextFile(true);
		fileInfoBuilder.setUnixExternalFileAttributes(0644);
		fileInfoBuilder.setFileReadOnly(true);
		fileInfoBuilder.setFileIsRegular(true);
		writer.addDirectoryFileInfo(fileInfoBuilder.build());
		writer.writeFileDataPart(fileBytes, 0, fileBytes.length);
		long dataOffset = writer.finishFileData();
		System.out.println("wrote " + dataOffset + " header + file bytes");
		long endOffset = writer.finishZip();
		writer.close();
		System.out.println("wrote " + (endOffset - dataOffset) + " bytes for central-directory");
		System.out.println("wrote " + endOffset + " total bytes to " + tmpFile);

		// now try to read it back in with the jdk stuff
		ZipFileReader reader = new ZipFileReader(tmpFile);
		assertNotNull(reader.readFileHeader());
		assertEquals(fileBytes.length, reader.skipFileData());
		assertNull(reader.readFileHeader());
		CentralDirectoryFileHeader dirHeader = reader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		reader.close();
	}
}
