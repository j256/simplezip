package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;

public class ZipFileOutputTest {

	@Test
	public void testStuff() throws IOException {

		String fileName1 = "hello";
		byte[] fileBytes1 = new byte[] { 1, 2, 3 };
		byte[] fileBytes2 = new byte[] { 21, 32, 35, 99, 19, 12, 127 };

		/*
		 * Write out our zip-file.
		 */

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		assertEquals(0, output.getNumBytesWritten());

		ZipFileHeader.Builder fileHeaderBuilder = ZipFileHeader.builder();
		fileHeaderBuilder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL,
				GeneralPurposeFlag.DATA_DESCRIPTOR);
		fileHeaderBuilder.setCompressionMethod(CompressionMethod.DEFLATED);
		fileHeaderBuilder.setLastModifiedDateTime(LocalDateTime.now());
		fileHeaderBuilder.setFileName(fileName1);
		output.writeFileHeader(fileHeaderBuilder.build());
		output.writeFileDataPart(fileBytes1);
		output.finishFileData();
		assertNotEquals(0, output.getNumBytesWritten());

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
		output.writeFileHeader(fileHeaderBuilder.build());
		output.writeFileDataPart(fileBytes2);
		output.finishFileData();
		// double finish
		output.finishZip();
		output.finishZip();
		output.flush();
		output.close();
		System.out.println("wrote " + output.getNumBytesWritten() + " bytes to zip output");

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
		new ZipFileOutput(new File("/doesnotexist")).close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testWriteToBadPath() throws IOException {
		new ZipFileOutput("/doesnotexist").close();
	}

	@Test
	public void testWriteToNullFile() throws IOException {
		new ZipFileOutput(new File("/dev/null")).close();
	}

	@Test
	public void testWriteToNullPath() throws IOException {
		new ZipFileOutput("/dev/null").close();
	}

	@Test
	public void testReadEmptyZip() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ZipFileOutput(baos).close();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));) {
			assertNull(zis.getNextEntry());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.close();
		output.writeFileHeader(ZipFileHeader.builder().build());
	}

	@Test(expected = IllegalStateException.class)
	public void testAddFileInfoAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.close();
		output.addDirectoryFileInfo(ZipCentralDirectoryFileInfo.builder().build());
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteFileDataAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.close();
		output.writeFileDataPart(new byte[0]);
	}

	@Test(expected = IllegalStateException.class)
	public void testFinishFileDataAfterClose() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.close();
		output.finishFileData();
	}

	@Test
	public void testCloseWithoutFnishingFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.writeFileHeader(ZipFileHeader.builder().build());
		output.writeFileDataPart(new byte[] { 1, 2, 3 });
		output.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteNextHeaderWithoutFnishingFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.writeFileHeader(ZipFileHeader.builder().build());
		output.writeFileHeader(ZipFileHeader.builder().build());
		output.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteDataWithoutHeader() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.writeFileDataPart(new byte[0]);
		output.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteRawDataWithoutHeader() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.writeRawFileDataPart(new byte[0]);
		output.close();
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
		ZipFileOutput output = new ZipFileOutput(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setFileName(file.getName());
		output.writeFileHeader(builder.build());
		output.writeFileData(file);
		output.writeFileHeader(builder.build());
		output.writeFileData(file.getPath());
		output.writeFileHeader(builder.build());
		output.writeFileDataPart(fileBytes);
		output.finishFileData();
		output.close();
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
		ZipFileOutput output = new ZipFileOutput(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName());
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32Value(crc32);
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file));
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file.getPath()));
		System.out.println("wrote end, offset = " + output.finishZip());
		output.close();
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
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(10240, 10240);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file));
		builder.setFileName(file.getName() + "2");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file.getPath()));
		System.out.println("wrote end, offset = " + output.finishZip());
		output.close();
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
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(10240, 2);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file));
		builder.setFileName(file.getName() + "2");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFileData(file.getPath()));
		System.out.println("wrote end, offset = " + output.finishZip());
		output.flush();
		output.close();
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
		ZipFileOutput output = new ZipFileOutput(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		String fileName = "hello.txt";
		builder.setFileName(fileName);
		output.writeFileHeader(builder.build());
		ZipCentralDirectoryFileInfo.Builder fileInfoBuilder = ZipCentralDirectoryFileInfo.builder();
		String comment = "hrm a nice lookin' file";
		fileInfoBuilder.setComment(comment);
		output.addDirectoryFileInfo(fileInfoBuilder.build());
		output.writeFileDataPart(fileBytes, 0, 1);
		output.writeFileDataPart(fileBytes, 1, fileBytes.length - 1);
		output.finishFileData();
		// do it twice
		assertTrue(output.addDirectoryFileInfo(fileName, fileInfoBuilder.build()));
		assertFalse(output.addDirectoryFileInfo("unknown-file", fileInfoBuilder.build()));
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNotNull(input.readFileHeader());
		assertEquals(fileBytes.length, input.skipFileData());
		assertNull(input.readFileHeader());
		ZipCentralDirectoryFileEntry dirHeader = input.readDirectoryFileEntry();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		input.close();
	}

	@Test
	public void testEndComment() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		String comment = "the very end";
		output.finishZip(ZipCentralDirectoryEndInfo.builder().withComment(comment).build());
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNull(input.readFileHeader());
		assertNull(input.readDirectoryFileEntry());
		ZipCentralDirectoryEnd end = input.readDirectoryEnd();
		assertEquals(comment, end.getComment());
		input.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testUnknownMethod() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setFileName("hello");
		builder.setCompressionMethod(CompressionMethod.IBM_TERSE);
		output.writeFileHeader(builder.build());
		output.writeFileDataPart(new byte[0]);
		output.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testAddFileInfoAfterFinished() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setFileName("hello");
		output.writeFileHeader(builder.build());
		output.writeFileDataPart(new byte[0]);
		output.close();
		output.addDirectoryFileInfo(ZipCentralDirectoryFileInfo.builder().build());
	}

	@Test(expected = IllegalStateException.class)
	public void testAddFileStringInfoAfterFinished() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setFileName("hello");
		output.writeFileHeader(builder.build());
		output.writeFileDataPart(new byte[0]);
		output.close();
		output.addDirectoryFileInfo("foo", ZipCentralDirectoryFileInfo.builder().build());
	}

	@Test
	public void testFileInAFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024 * 1024, 1024 * 1024);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		String outerFileName = "outer.bin";
		builder.setFileName(outerFileName);
		output.writeFileHeader(builder.build());
		byte[] outerFileBytes = new byte[] { 1, 2, 3, 4, 5, 6 };
		output.writeFileDataPart(outerFileBytes);
		output.finishFileData();

		String zipName = "hello.zip";
		builder.setFileName(zipName);
		output.writeFileHeader(builder.build());

		// write an inner zip file
		ZipFileOutput innerOutput = new ZipFileOutput(output.openFileDataOutputStream(false));
		innerOutput.enableFileBuffering(1024 * 1024, 1024 * 1024);
		String innerFileName = "inner.bin";
		builder.setFileName(innerFileName);
		innerOutput.writeFileHeader(builder.build());
		byte[] innerFileBytes = new byte[] { 10, 20, 30, 40, 50, 60 };
		innerOutput.writeFileDataPart(innerFileBytes);
		innerOutput.finishFileData();
		innerOutput.close();

		output.close();

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
		ZipFileInput input = new ZipFileInput(bais);
		ZipFileHeader header = input.readFileHeader();
		assertEquals(outerFileName, header.getFileName());
		num = input.readFileDataPart(buffer);
		assertEquals(outerFileBytes.length, num);
		assertArrayEquals(outerFileBytes, Arrays.copyOf(buffer, num));
		assertEquals(-1, input.readFileDataPart(buffer));

		header = input.readFileHeader();
		assertNotNull(header);
		assertEquals(zipName, header.getFileName());
		// process inner zip file
		ZipFileInput innerReader = new ZipFileInput(input.openFileDataInputStream(false));
		header = innerReader.readFileHeader();
		assertEquals(innerFileName, header.getFileName());
		num = innerReader.readFileDataPart(buffer);
		assertArrayEquals(innerFileBytes, Arrays.copyOf(buffer, num));
		assertEquals(-1, innerReader.readFileDataPart(buffer));
		assertNull(innerReader.readFileHeader());
		ZipCentralDirectoryFileEntry dirHeader = innerReader.readDirectoryFileEntry();
		assertNotNull(dirHeader);
		assertEquals(innerFileName, dirHeader.getFileName());
		assertNull(innerReader.readDirectoryFileEntry());

		// NOTE we need to make sure that we read all of the bytes from the inner file
		innerReader.close();
		innerReader = null;

		assertNull(input.readFileHeader());
		dirHeader = input.readDirectoryFileEntry();
		assertNotNull(dirHeader);
		assertEquals(outerFileName, dirHeader.getFileName());

		input.close();
	}

	@Test
	public void testRawInputOutput() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024 * 1024, 1024 * 1024);
		String fileName = "outer.bin";
		output.writeFileHeader(
				ZipFileHeader.builder().withFileName(fileName).withCompressionMethod(CompressionMethod.STORED).build());
		byte[] fileBytes1 = new byte[] { 1, 2, 3 };
		output.writeRawFileDataPart(fileBytes1);
		expected.write(fileBytes1);
		byte[] fileBytes2 = new byte[] { 1, 2, 3, 4, 5, 6 };
		OutputStream fos = output.openFileDataOutputStream(true);
		fos.write(fileBytes2[0]);
		fos.write(fileBytes2, 1, fileBytes2.length - 1);
		expected.write(fileBytes2);
		fos.flush();
		output.finishFileData();
		output.close();

		/*
		 * Read it back in.
		 */

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// read using ZipInputStream
		ZipFileInput zipInput = new ZipFileInput(bais);
		ZipFileHeader entry = zipInput.readFileHeader();
		assertEquals(fileName, entry.getFileName());
		byte[] output1 = new byte[fileBytes1.length];
		zipInput.readRawFileDataPart(output1);
		InputStream fis = zipInput.openFileDataInputStream(true);
		baos.reset();
		baos.write(output1);
		baos.write(fis.read());
		IoUtils.copyStream(fis, baos);
		assertArrayEquals(expected.toByteArray(), baos.toByteArray());
		IoUtils.copyStream(fis, baos);
		zipInput.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnable() throws IOException {
		ZipFileOutput zipOutput = new ZipFileOutput(new ByteArrayOutputStream());
		zipOutput.enableFileBuffering(1, 2);
		zipOutput.close();
	}

	@Test
	public void testLargerFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		byte[] buffer = new byte[IoUtils.STANDARD_BUFFER_SIZE * 2];
		new Random().nextBytes(buffer);
		String fileName = "bar.txt";
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName).build());
		zipOutput.writeFileDataAll(buffer);
		zipOutput.close();

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
		ZipEntry entry = zis.getNextEntry();
		assertEquals(fileName, entry.getName());
		baos.reset();
		byte[] readBuffer = new byte[1024];
		while (true) {
			int numRead = zis.read(readBuffer);
			if (numRead < 0) {
				break;
			}
			baos.write(readBuffer, 0, numRead);
		}
		assertArrayEquals(buffer, baos.toByteArray());
	}

	@Test
	public void testBufferedFileNoDataDescriptor() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		zipOutput.enableFileBuffering(Integer.MAX_VALUE, Integer.MAX_VALUE);
		byte[] buffer = new byte[1024];
		new Random().nextBytes(buffer);
		String fileName1 = "bar.txt";
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName1).build());
		zipOutput.writeFileDataAll(buffer);
		String fileName2 = "bar2.txt";
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName2).build());
		zipOutput.writeFileDataAll(buffer);
		zipOutput.close();

		ZipFileInput zipInput = new ZipFileInput(new ByteArrayInputStream(baos.toByteArray()));
		ZipFileHeader header = zipInput.readFileHeader();
		assertEquals(fileName1, header.getFileName());
		assertFalse(header.getGeneralPurposeFlagsAsEnums().contains(GeneralPurposeFlag.DATA_DESCRIPTOR));
		baos.reset();
		zipInput.readFileData(baos);
		assertArrayEquals(buffer, baos.toByteArray());
		header = zipInput.readFileHeader();
		assertEquals(fileName2, header.getFileName());

		zipInput.close();
	}

	@Test
	public void testFinishWithoutData() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		String fileName1 = "boo.txt";
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName1).build());
		zipOutput.finishFileData();
		zipOutput.close();

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
		assertNotNull(zis.getNextEntry());
		assertNull(zis.getNextEntry());
		zis.close();
	}

	@Test
	public void testManyManyFiles() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		int numFiles = IoUtils.MAX_UNSIGNED_SHORT_VALUE + 1;
		for (int i = 0; i < numFiles; i++) {
			String fileName1 = "bar" + i + ".txt";
			zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName1).build());
			zipOutput.finishFileData();
		}
		zipOutput.close();

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
		int fileCount = 0;
		while (true) {
			if (zis.getNextEntry() == null) {
				break;
			}
			fileCount++;
		}
		assertEquals(numFiles, fileCount);
		zis.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteAfterFinished() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		String fileName1 = "barbing.txt";
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName(fileName1).build());
		zipOutput.finishFileData();
		zipOutput.close();
		zipOutput.writeFileDataPart(new byte[] { 1, 2, 3 });
	}

	public static void main(String[] args) throws IOException {

		byte[] fileBytes =
				"let's see what zip does with this string to compress.  can it compress it more than 0%".getBytes();

		File tmpFile = new File("/tmp/x.zip");
		ZipFileOutput output = new ZipFileOutput(tmpFile);
		output.enableFileBuffering(10240, 10240);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setLastModifiedDateTime(LocalDateTime.now());
		builder.setFileName("hello");
		output.writeFileHeader(builder.build());
		ZipCentralDirectoryFileInfo.Builder fileInfoBuilder = ZipCentralDirectoryFileInfo.builder();
		String comment = "hrm a nice lookin' file";
		fileInfoBuilder.setComment(comment);
		fileInfoBuilder.setTextFile(true);
		fileInfoBuilder.setUnixExternalFileAttributes(0644);
		fileInfoBuilder.setFileIsReadOnly(true);
		fileInfoBuilder.setFileIsRegular(true);
		output.addDirectoryFileInfo(fileInfoBuilder.build());
		output.writeFileDataPart(fileBytes, 0, fileBytes.length);
		long dataOffset = output.finishFileData();
		System.out.println("wrote " + dataOffset + " header + file bytes");
		long endOffset = output.finishZip();
		output.close();
		System.out.println("wrote " + (endOffset - dataOffset) + " bytes for central-directory");
		System.out.println("wrote " + endOffset + " total bytes to " + tmpFile);

		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(tmpFile);
		assertNotNull(input.readFileHeader());
		assertEquals(fileBytes.length, input.skipFileData());
		assertNull(input.readFileHeader());
		ZipCentralDirectoryFileEntry dirHeader = input.readDirectoryFileEntry();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		input.close();
	}
}
