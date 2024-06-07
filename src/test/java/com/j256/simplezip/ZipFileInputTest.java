package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipDataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;

public class ZipFileInputTest {

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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		assertFalse(input.assignDirectoryFileEntryPermissions());
		assertEquals(0, input.getNumBytesRead());

		assertNull(input.getCurrentFileName());
		ZipFileHeader header = input.readFileHeader();
		assertEquals(input.getCurrentFileName(), header.getFileName());
		assertEquals(fileName, header.getFileName());
		// there is no sizes because there is data-descriptor because we were streaming data in
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertEquals(0, header.getCrc32());
		assertTrue(header.getGeneralPurposeFlagsAsEnums().contains(GeneralPurposeFlag.DATA_DESCRIPTOR));
		assertEquals(CompressionMethod.DEFLATED, header.getCompressionMethodAsEnum());

		System.out.println("header " + header.getFileName() + ", date " + header.getLastModifiedDateTime() + ", time "
				+ header.getLastModifiedTimeString() + ", size " + header.getUncompressedSize() + ", method "
				+ header.getCompressionMethod() + ", extra " + Arrays.toString(header.getExtraFieldBytes()));

		baos.reset();
		// empty read
		assertEquals(0, input.readFileDataPart(new byte[0]));
		long numRead = input.readFileData(baos);
		assertEquals(bytes.length, numRead);
		assertNotEquals(0, input.getCurrentFileCountingInfo().getCrc32());
		assertArrayEquals(bytes, baos.toByteArray());

		ZipDataDescriptor dataDescriptor = input.getCurrentDataDescriptor();
		assertNotNull(dataDescriptor);
		assertEquals(bytes.length, dataDescriptor.getUncompressedSize());
		CRC32 crc32 = new CRC32();
		crc32.update(bytes);
		assertEquals(crc32.getValue(), dataDescriptor.getCrc32());

		assertNull(input.readFileHeader());

		ZipCentralDirectoryFileEntry dirHeader = input.readDirectoryFileEntry();
		assertNotNull(dirHeader);
		System.out.println("dir " + dirHeader.getFileName() + ", size " + dirHeader.getUncompressedSize() + ", method "
				+ dirHeader.getCompressionMethod() + ", extra " + Arrays.toString(dirHeader.getExtraFieldBytes()));

		assertNull(input.readDirectoryFileEntry());
		ZipCentralDirectoryEnd end = input.readDirectoryEnd();
		assertNotNull(end);
		System.out.println("end: num-records " + end.getNumRecordsTotal() + ", size " + end.getDirectorySize());

		input.close();
		assertNotEquals(0, input.getNumBytesRead());
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromBadFile() throws IOException {
		new ZipFileInput(new File("/doesnotexist")).close();
	}

	@Test(expected = FileNotFoundException.class)
	public void testReadFromBadPath() throws IOException {
		new ZipFileInput("/doesnotexist").close();
	}

	@Test
	public void testReadFromNullFile() throws IOException {
		new ZipFileInput(new File("/dev/null")).close();
	}

	@Test
	public void testReadFromNullPath() throws IOException {
		new ZipFileInput("/dev/null").close();
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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = input.readFileDataPart(buffer);
		assertEquals(bytes.length, num);
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));
		input.close();
	}

	@Test(expected = IllegalStateException.class)
	public void readFileDataWithoutHeader() throws IOException {
		ZipFileInput input = new ZipFileInput(new ByteArrayInputStream(new byte[0]));
		input.readFileDataPart(new byte[0]);
		input.close();
	}

	@Test(expected = IllegalStateException.class)
	public void readRawFileDataWithoutHeader() throws IOException {
		ZipFileInput input = new ZipFileInput(new ByteArrayInputStream(new byte[0]));
		input.readRawFileDataPart(new byte[0]);
		input.close();
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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		byte[] buffer = new byte[1024];
		assertEquals(-1, input.readFileDataPart(buffer));
		assertEquals(-1, input.readFileDataPart(buffer));
		input.close();
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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = input.readFileDataPart(buffer, 0, 1);
		assertEquals(1, num);
		assertFalse(input.isFileDataEofReached());
		num = input.readFileDataPart(buffer, 1, buffer.length - 1);
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num + 1));
		input.close();
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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		ZipFileHeader header = input.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		byte[] buffer = new byte[1024];
		int num = input.readFileDataPart(buffer, 0, buffer.length);
		assertEquals(bytes.length, num);
		assertEquals(-1, input.readFileDataPart(buffer, 0, buffer.length));
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));
		input.close();
	}

	@Test
	public void testReadWithInputStream() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ZipFileOutput ouput = new ZipFileOutput(baos);
		ouput.enableFileBuffering(10240, 10240);
		String fileName = "hello";
		Builder builder = ZipFileHeader.builder().withFileName(fileName).withCompressionMethod(CompressionMethod.NONE);
		byte[] bytes = new byte[] { 1, 2, 3 };
		ouput.writeFileHeader(builder.build());
		ouput.writeFileDataPart(bytes);
		ouput.finishFileData();
		ouput.close();

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		ZipFileHeader header = input.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		byte[] buffer = new byte[1024];
		InputStream fileStream = input.openFileDataInputStream(false);
		assertSame(fileStream, input.openFileDataInputStream(false));
		int first = fileStream.read();
		int num = fileStream.read(buffer, 0, buffer.length - 1);
		assertEquals(bytes.length, num + 1);
		assertEquals(-1, fileStream.read());
		assertEquals(bytes[0], first);
		assertArrayEquals(Arrays.copyOfRange(bytes, 1, bytes.length), Arrays.copyOf(buffer, num));
		input.close();
	}

	@Test
	public void testReadPartialWithInputStream() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ZipFileOutput ouput = new ZipFileOutput(baos);
		ouput.enableFileBuffering(10240, 10240);
		String fileName = "hello";
		Builder builder = ZipFileHeader.builder().withFileName(fileName).withCompressionMethod(CompressionMethod.NONE);
		byte[] bytes = new byte[] { 1, 2, 3 };
		ouput.writeFileHeader(builder.build());
		ouput.writeFileDataPart(bytes);
		ouput.finishFileData();
		ouput.close();

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		input.setReadTillEof(false);
		ZipFileHeader header = input.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		input.close();
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

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		ZipFileHeader header = input.readFileHeader();
		assertEquals(CompressionMethod.NONE, header.getCompressionMethodAsEnum());
		byte[] buffer = new byte[1024];
		assertEquals(-1, input.readFileDataPart(buffer, 0, buffer.length));
		assertEquals(-1, input.readFileDataPart(buffer, 0, buffer.length));
		input.close();
	}

	@Test(expected = IOException.class)
	public void testWriteBadDeflateData() throws IOException {
		byte[] fileBytes = new byte[] { (byte) 200, (byte) 200, 2, 1 };
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.DEFLATED);
		String name = "hello.bin";
		builder.setFileName(name);
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32(crc32.getValue());
		output.writeFileHeader(builder.build());
		output.writeRawFileDataPart(fileBytes);
		System.out.println("wrote raw file, offset = " + output.finishFileData());
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		ZipFileHeader header = input.readFileHeader();
		assertNotNull(header);
		assertEquals(name, header.getFileName());
		byte[] buffer = new byte[1024];
		input.readFileDataPart(buffer);
		input.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteUnknownMethod() throws IOException {
		byte[] fileBytes = new byte[] { (byte) 200, (byte) 200, 2, 1 };
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		Builder builder = ZipFileHeader.builder();
		builder.setCompressionMethod(CompressionMethod.IBM_TERSE);
		String name = "hello.bin";
		builder.setFileName(name);
		builder.setCompressedSize(fileBytes.length);
		builder.setUncompressedSize(fileBytes.length);
		CRC32 crc32 = new CRC32();
		crc32.update(fileBytes);
		builder.setCrc32(crc32.getValue());
		output.writeFileHeader(builder.build());
		output.writeRawFileDataPart(fileBytes);
		System.out.println("wrote raw file, offset = " + output.finishFileData());
		output.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		ZipFileHeader header = input.readFileHeader();
		assertNotNull(header);
		assertEquals(name, header.getFileName());
		byte[] buffer = new byte[1024];
		input.readFileDataPart(buffer);
		input.close();
	}

	@Test
	public void testReadRaw() throws IOException, DataFormatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipFileHeader fileHeader = ZipFileHeader.builder().withFileName(fileName).build();
		byte[] bytes = new byte[] { 1, 2, 3 };
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		zipOutput.enableFileBuffering(10240, 10240);
		zipOutput.writeFileHeader(fileHeader);
		zipOutput.writeFileDataPart(bytes);
		zipOutput.finishFileData();
		zipOutput.close();

		RewindableInputStream inputStream =
				new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240);
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		baos.reset();
		input.readRawFileData(baos);

		// now we inflate the data externally
		Inflater inflater = new Inflater(true /* no wrap */);
		inflater.setInput(baos.toByteArray(), 0, baos.size());
		byte[] output = new byte[1024];
		int numInflated = inflater.inflate(output, 0, output.length);
		assertTrue(inflater.finished());
		inflater.end();
		assertArrayEquals(bytes, Arrays.copyOf(output, numInflated));

		input.close();
	}

	@Test
	public void testReadToFile() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName1 = "target/hello1.t";
		ZipFileHeader fileHeader = ZipFileHeader.builder().withFileName(fileName1).build();
		byte[] bytes = new byte[] { 1, 2, 3 };
		ZipFileOutput zipOutput = new ZipFileOutput(baos);
		zipOutput.enableFileBuffering(10240, 10240);
		zipOutput.writeFileHeader(fileHeader);
		zipOutput.writeFileDataAll(bytes);
		// write another file
		String fileName2 = "target/hello2.t";
		fileHeader = ZipFileHeader.builder().withFileName(fileName2).build();
		zipOutput.writeFileHeader(fileHeader);
		zipOutput.writeFileDataAll(bytes);
		zipOutput.close();

		RewindableInputStream inputStream =
				new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240);
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		File file1 = new File(fileName1);
		file1.getParentFile().mkdirs();
		file1.deleteOnExit();
		input.readFileDataToFile(file1.getPath());
		byte[] output = readFileToBytes(file1);
		assertArrayEquals(bytes, output);
		try {
			input.assignDirectoryFileEntryPermissions();
			fail("should have thrown");
		} catch (IllegalStateException ise) {
			// ignore
		}

		assertNotNull(input.readFileHeader());
		byte[] buffer = new byte[1024];
		int num = input.readFileDataPart(buffer);
		assertArrayEquals(bytes, Arrays.copyOf(buffer, num));

		assertNull(input.readFileHeader());
		assertFalse(input.readDirectoryFileEntriesAndAssignPermissions());

		file1.delete();

		input.close();
	}

	@Test
	public void testReadJustHeaders() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "hello";
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(new ZipEntry(fileName));
		byte[] bytes = new byte[] { 1, 2, 3 };
		zos.write(bytes);
		zos.closeEntry();
		zos.putNextEntry(new ZipEntry(fileName + "2"));
		bytes = new byte[] { 3, 2, 1 };
		zos.write(bytes);
		zos.closeEntry();
		zos.close();

		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		ZipFileInput input = new ZipFileInput(inputStream);
		assertNotNull(input.readFileHeader());
		assertNotNull(input.readFileHeader());
		assertNull(input.readFileHeader());
		input.close();
	}

	@Test
	public void testReadFromFile() throws IOException {
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".t");
		tmpFile.deleteOnExit();
		String fileName = "hello";
		ZipFileOutput zipOutput = new ZipFileOutput(tmpFile);
		zipOutput.enableFileBuffering(1024, 1024);
		ZipFileHeader entry = ZipFileHeader.builder().withFileName(fileName).build();
		zipOutput.writeFileHeader(entry);
		byte[] bytes = new byte[] { 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3 };
		zipOutput.writeFileDataAll(bytes);
		entry = ZipFileHeader.builder().withFileName(fileName + "2").build();
		zipOutput.writeFileHeader(entry);
		zipOutput.writeFileDataAll(bytes);
		zipOutput.close();

		ZipFileInput input = new ZipFileInput(tmpFile);
		ZipFileHeader header = input.readFileHeader();
		assertNotNull(header);
		System.out.println("header " + header);
		assertNotNull(input.readFileHeader());
		assertNull(input.readFileHeader());
		input.close();
		tmpFile.delete();
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
