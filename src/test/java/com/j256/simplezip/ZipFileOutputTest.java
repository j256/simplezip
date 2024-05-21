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

public class ZipFileOutputTest {

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
		output.addDirectoryFileInfo(CentralDirectoryFileInfo.builder().build());
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

	@Test(expected = IllegalStateException.class)
	public void testCloseWithoutFnishingFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.writeFileHeader(ZipFileHeader.builder().build());
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
		output.writeFile(file);
		output.writeFileHeader(builder.build());
		output.writeFile(file.getPath());
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
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file));
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file.getPath()));
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
		output.enableBufferedOutput(10240, 10240);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file));
		builder.setFileName(file.getName() + "2");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file.getPath()));
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
		output.enableBufferedOutput(10240, 2);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.NONE);
		builder.setFileName(file.getName() + "1");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file));
		builder.setFileName(file.getName() + "2");
		output.writeFileHeader(builder.build());
		System.out.println("wrote raw file, offset = " + output.writeRawFile(file.getPath()));
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
		builder.setFileName("hello");
		output.writeFileHeader(builder.build());
		CentralDirectoryFileInfo.Builder fileInfoBuilder = CentralDirectoryFileInfo.builder();
		String comment = "hrm a nice lookin' file";
		fileInfoBuilder.setComment(comment);
		output.addDirectoryFileInfo(fileInfoBuilder.build());
		output.writeFileDataPart(fileBytes, 0, 1);
		output.writeFileDataPart(fileBytes, 1, fileBytes.length - 1);
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNotNull(input.readFileHeader());
		assertEquals(fileBytes.length, input.skipFileData());
		assertNull(input.readFileHeader());
		CentralDirectoryFileHeader dirHeader = input.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		input.close();
	}

	@Test
	public void testEndComment() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		String comment = "the very end";
		output.finishZip(comment);
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNull(input.readFileHeader());
		assertNull(input.readDirectoryFileHeader());
		CentralDirectoryEnd end = input.readDirectoryEnd();
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

	@Test
	public void testFileInAFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableBufferedOutput(1024 * 1024, 1024 * 1024);
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
		innerOutput.enableBufferedOutput(1024 * 1024, 1024 * 1024);
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
		CentralDirectoryFileHeader dirHeader = innerReader.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(innerFileName, dirHeader.getFileName());
		assertNull(innerReader.readDirectoryFileHeader());

		// NOTE we need to make sure that we read all of the bytes from the inner file
		innerReader.close();
		innerReader = null;

		assertNull(input.readFileHeader());
		dirHeader = input.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(outerFileName, dirHeader.getFileName());

		input.close();
	}

	public static void main(String[] args) throws IOException {

		byte[] fileBytes =
				"let's see what zip does with this string to compress.  can it compress it more than 0%".getBytes();

		File tmpFile = new File("/tmp/x.zip");
		ZipFileOutput output = new ZipFileOutput(tmpFile);
		output.enableBufferedOutput(10240, 10240);
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setLastModifiedDateTime(LocalDateTime.now());
		builder.setFileName("hello");
		output.writeFileHeader(builder.build());
		CentralDirectoryFileInfo.Builder fileInfoBuilder = CentralDirectoryFileInfo.builder();
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
		CentralDirectoryFileHeader dirHeader = input.readDirectoryFileHeader();
		assertNotNull(dirHeader);
		assertEquals(comment, dirHeader.getComment());
		input.close();
	}
}
