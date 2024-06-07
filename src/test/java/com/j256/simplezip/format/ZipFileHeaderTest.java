package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.ZipFileHeader.Builder;
import com.j256.simplezip.format.extra.UnknownExtraField;
import com.j256.simplezip.format.extra.Zip64ExtraField;

public class ZipFileHeaderTest {

	@Test
	public void testDateTime() {
		LocalDateTime input;
		do {
			input = LocalDateTime.now();
			input = input.truncatedTo(ChronoUnit.SECONDS);
		} while (input.getSecond() % 2 != 0);

		Builder builder = ZipFileHeader.builder();
		builder.setLastModifiedDateTime(input);
		int date = builder.getLastModifiedDate();
		int time = builder.getLastModifiedTime();
		// coverage
		builder.withLastModifiedDateTime(input);
		ZipFileHeader header = builder.build();

		LocalDateTime output = header.getLastModifiedDateTime();
		System.out.println("last-mod date is: " + header.getLastModifiedDate());
		System.out.println("last-mod time is: " + header.getLastModifiedTime());
		assertEquals(input, output);
		assertEquals(date, header.getLastModifiedDate());
		assertEquals(time, header.getLastModifiedTime());
	}

	@Test
	public void testDateTimeMillis() throws IOException {
		LocalDateTime input;
		do {
			input = LocalDateTime.now();
			input = input.truncatedTo(ChronoUnit.SECONDS);
		} while (input.getSecond() % 2 != 0);

		ZonedDateTime zdt = input.atZone(ZoneId.systemDefault());
		long millis = zdt.toInstant().toEpochMilli();

		Builder builder = ZipFileHeader.builder();
		builder.withLastModifiedDateTime(millis);
		int date = builder.getLastModifiedDate();
		int time = builder.getLastModifiedTime();
		// coverage
		builder.withLastModifiedDateTime(input);
		ZipFileHeader header = builder.build();

		LocalDateTime output = header.getLastModifiedDateTime();
		System.out.println("last-mod date is: " + header.getLastModifiedDate());
		System.out.println("last-mod time is: " + header.getLastModifiedTime());
		assertEquals(input, output);
		assertEquals(date, header.getLastModifiedDate());
		assertEquals(time, header.getLastModifiedTime());

		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".t");
		tmpFile.deleteOnExit();
		tmpFile.setLastModified(millis);
		date = builder.getLastModifiedDate();
		time = builder.getLastModifiedTime();
		builder.setLastModifiedDateTime(tmpFile);
		assertEquals(date, builder.getLastModifiedDate());
		assertEquals(time, builder.getLastModifiedTime());
		builder.withLastModifiedDateTime(tmpFile);
		assertEquals(date, builder.getLastModifiedDate());
		assertEquals(time, builder.getLastModifiedTime());
		tmpFile.delete();
	}

	@Test
	public void testCoverage() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();

		int versionNeeded = 5251312;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		// need to handle the logic of the data-descriptor
		int generalPurposeFlags = 565479567 & ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		builder.setGeneralPurposeFlags(generalPurposeFlags);
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		int compressionMethod = 6334324;
		builder.setCompressionMethod(compressionMethod);
		assertEquals(compressionMethod, builder.getCompressionMethod());
		CompressionMethod compressionMethodEnum = CompressionMethod.DEFLATED;
		builder.setCompressionMethod(compressionMethodEnum);
		assertEquals(compressionMethodEnum, builder.getCompressionMethodAsEnum());
		int lastModifiedFileTime = 29912;
		builder.setLastModifiedTime(lastModifiedFileTime);
		assertEquals(lastModifiedFileTime, builder.getLastModifiedTime());
		int lastModifiedFileDate = 22708;
		builder.setLastModifiedDate(lastModifiedFileDate);
		assertEquals(lastModifiedFileDate, builder.getLastModifiedDate());
		int crc32 = 654654;
		builder.setCrc32(crc32);
		assertEquals(crc32, builder.getCrc32());
		int compressedSize = 42343423;
		builder.setCompressedSize(compressedSize);
		assertEquals(compressedSize, builder.getCompressedSize());
		int uncompressedSize = 65654568;
		builder.setUncompressedSize(uncompressedSize);
		assertEquals(uncompressedSize, builder.getUncompressedSize());
		assertNull(builder.getFileName());
		String fileName = "hfewoifhewf";
		builder.setFileName(fileName);
		assertEquals(fileName, builder.getFileName());
		fileName = "hello.txt";
		builder.withFileName(fileName);
		assertEquals(fileName, builder.getFileName());
		byte[] fileNameBytes = fileName.getBytes();
		builder.setFileNameBytes(fileNameBytes);
		assertArrayEquals(fileNameBytes, builder.getFileNameBytes());
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.setExtraFieldBytes(extraBytes);
		assertEquals(extraBytes, builder.getExtraFieldBytes());

		ZipFileHeader header = builder.build();
		assertEquals(compressionMethodEnum, header.getCompressionMethodAsEnum());
		assertEquals(compressionMethodEnum.getValue(), header.getCompressionMethod());
		assertEquals(versionNeeded, header.getVersionNeeded());
		assertEquals((versionNeeded & 0xFF), header.getVersionNeededMajorMinor());
		assertEquals("24.0", header.getVersionNeededMajorMinorString());
		assertEquals(generalPurposeFlags, header.getGeneralPurposeFlags());
		assertEquals(lastModifiedFileTime, header.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, header.getLastModifiedDate());
		assertEquals(crc32, header.getCrc32());
		assertEquals(compressedSize, header.getCompressedSize());
		assertEquals(compressedSize, header.getZip64CompressedSize());
		assertEquals(uncompressedSize, header.getUncompressedSize());
		assertEquals(uncompressedSize, header.getZip64UncompressedSize());
		assertArrayEquals(fileNameBytes, header.getFileNameBytes());
		assertArrayEquals(extraBytes, header.getExtraFieldBytes());

		System.out.println("last-mod date is: " + header.getLastModifiedDateString());
		System.out.println("last-mod time is: " + header.getLastModifiedTimeString());
		System.out.println("to-string is: " + header);

		Builder copyBuilder = Builder.fromHeader(header);
		ZipFileHeader copy = copyBuilder.build();
		assertEquals(compressionMethodEnum, copy.getCompressionMethodAsEnum());
		assertEquals(compressionMethodEnum.getValue(), copy.getCompressionMethod());
		assertEquals(versionNeeded, copy.getVersionNeeded());
		assertEquals(generalPurposeFlags, copy.getGeneralPurposeFlags());
		assertEquals(lastModifiedFileTime, copy.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, copy.getLastModifiedDate());
		assertEquals(crc32, copy.getCrc32());
		assertEquals(compressedSize, copy.getCompressedSize());
		assertEquals(uncompressedSize, copy.getUncompressedSize());
		assertArrayEquals(fileNameBytes, copy.getFileNameBytes());
		assertArrayEquals(extraBytes, copy.getExtraFieldBytes());

		copyBuilder.reset();
		copy = copyBuilder.build();
		assertEquals(CompressionMethod.DEFLATED, copy.getCompressionMethodAsEnum());
		assertEquals(CompressionMethod.DEFLATED.getValue(), copy.getCompressionMethod());
		assertEquals(0, copy.getVersionNeeded());
		assertEquals(0, copy.getGeneralPurposeFlags());
		assertEquals(0, copy.getLastModifiedTime());
		assertEquals(0, copy.getLastModifiedDate());
		assertEquals(0, copy.getCrc32());
		assertEquals(0, copy.getCompressedSize());
		assertEquals(0, copy.getUncompressedSize());
		assertArrayEquals(null, copy.getFileNameBytes());
		assertArrayEquals(null, copy.getExtraFieldBytes());
	}

	@Test
	public void testInvalidExtraField() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.setExtraFieldBytes(extraBytes);
		assertEquals(extraBytes, builder.getExtraFieldBytes());

		ZipFileHeader header = builder.build();
		assertArrayEquals(extraBytes, header.getExtraFieldBytes());
	}

	@Test
	public void testBuildWith() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();

		builder.setVersionNeededMajorMinor(1, 7);
		assertEquals(17, builder.getVersionNeeded());
		builder.withVersionNeededMajorMinor(2, 3);
		assertEquals(23, builder.getVersionNeeded());
		int versionNeeded = 5251312;
		builder.withVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		// need to handle the logic of the data-descriptor
		int generalPurposeFlags = 565479567 & ~GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		builder.withGeneralPurposeFlags(generalPurposeFlags);
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		int compressionMethod = 6334324;
		builder.withCompressionMethod(compressionMethod);
		assertEquals(compressionMethod, builder.getCompressionMethod());
		CompressionMethod compressionMethodEnum = CompressionMethod.DEFLATED;
		builder.withCompressionMethod(compressionMethodEnum);
		assertEquals(compressionMethodEnum, builder.getCompressionMethodAsEnum());
		int lastModifiedFileTime = 29912;
		builder.withLastModifiedTime(lastModifiedFileTime);
		assertEquals(lastModifiedFileTime, builder.getLastModifiedTime());
		int lastModifiedFileDate = 22708;
		builder.withLastModifiedDate(lastModifiedFileDate);
		assertEquals(lastModifiedFileDate, builder.getLastModifiedDate());
		int crc32 = 654654;
		builder.withCrc32(crc32);
		assertEquals(crc32, builder.getCrc32());
		int compressedSize = 42343423;
		builder.withCompressedSize(compressedSize);
		assertEquals(compressedSize, builder.getCompressedSize());
		int uncompressedSize = 65654568;
		builder.withUncompressedSize(uncompressedSize);
		assertEquals(uncompressedSize, builder.getUncompressedSize());
		byte[] fileNameBytes = new byte[] { 4, 5, 1, 1, 3, 5 };
		builder.withFileNameBytes(fileNameBytes);
		assertArrayEquals(fileNameBytes, builder.getFileNameBytes());
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.withExtraFieldBytes(extraBytes);
		assertEquals(extraBytes, builder.getExtraFieldBytes());

		ZipFileHeader header = builder.build();
		assertEquals(compressionMethodEnum, header.getCompressionMethodAsEnum());
		assertEquals(compressionMethodEnum.getValue(), header.getCompressionMethod());
		assertEquals(versionNeeded, header.getVersionNeeded());
		assertEquals(generalPurposeFlags, header.getGeneralPurposeFlags());
		assertEquals(lastModifiedFileTime, header.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, header.getLastModifiedDate());
		assertEquals(crc32, header.getCrc32());
		assertEquals(compressedSize, header.getCompressedSize());
		assertEquals(uncompressedSize, header.getUncompressedSize());
		assertArrayEquals(fileNameBytes, header.getFileNameBytes());
		assertArrayEquals(extraBytes, header.getExtraFieldBytes());
	}

	@Test
	public void testFlags() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();

		int flags = 0;
		builder.assignGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR, false);
		builder.assignGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR, true);
		flags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

		builder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.LANGUAGE_ENCODING);
		flags |= GeneralPurposeFlag.DEFLATING_NORMAL.getValue() | GeneralPurposeFlag.LANGUAGE_ENCODING.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

		builder.addGeneralPurposeFlags(Arrays.asList(GeneralPurposeFlag.ENCRYPTED));
		flags |= GeneralPurposeFlag.ENCRYPTED.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

		builder.withGeneralPurposeFlags(Arrays.asList(GeneralPurposeFlag.PKWARE1));
		flags |= GeneralPurposeFlag.PKWARE1.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());
	}

	@Test
	public void testcompressionLevelMin() {
		ZipFileHeader header =
				ZipFileHeader.builder().withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_SUPER_FAST).build();
		assertEquals(Deflater.BEST_SPEED, header.getCompressionLevel());
	}

	@Test
	public void testcompressionLevelFast() {
		ZipFileHeader header =
				ZipFileHeader.builder().withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_FAST).build();
		int level = (Deflater.DEFAULT_COMPRESSION + Deflater.BEST_SPEED) / 2;
		assertEquals(level, header.getCompressionLevel());
	}

	@Test
	public void testcompressionLevelMax() {
		ZipFileHeader header =
				ZipFileHeader.builder().withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_MAXIMUM).build();
		assertEquals(Deflater.BEST_COMPRESSION, header.getCompressionLevel());
	}

	@Test
	public void testReadWrite() throws IOException {
		int compressedSize = 14313;
		long crc32 = 423423423;
		Builder builder = ZipFileHeader.builder()
				.withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_SUPER_FAST)
				.withCompressedSize(compressedSize)
				.withCrc32(crc32);
		assertEquals(new HashSet<GeneralPurposeFlag>(Arrays.asList(GeneralPurposeFlag.DEFLATING_SUPER_FAST)),
				builder.getGeneralPurposeFlagAsEnums());
		ZipFileHeader header = builder.build();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		header.write(baos);
		ZipFileHeader result =
				ZipFileHeader.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024));
		assertTrue(result.hasFlag(GeneralPurposeFlag.DEFLATING_SUPER_FAST));
		assertFalse(result.hasFlag(GeneralPurposeFlag.COMPRESS_PATCHED));
		assertEquals(new HashSet<GeneralPurposeFlag>(Arrays.asList(GeneralPurposeFlag.DEFLATING_SUPER_FAST)),
				result.getGeneralPurposeFlagsAsEnums());
		assertEquals(1, result.getCompressionLevel());
		assertEquals(header.getCompressionLevel(), result.getCompressionLevel());
	}

	@Test
	public void testWriteNeedsDataDescriptor() throws IOException {
		ZipFileHeader header =
				ZipFileHeader.builder().withGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL).build();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		header.write(baos);
		ZipFileHeader result =
				ZipFileHeader.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024));
		assertEquals(new HashSet<GeneralPurposeFlag>(Arrays.asList(GeneralPurposeFlag.DATA_DESCRIPTOR)),
				result.getGeneralPurposeFlagsAsEnums());
	}

	@Test
	public void testHasFlags() {
		Builder builder = ZipFileHeader.builder();
		ZipFileHeader header = builder.build();
		assertEquals(Deflater.DEFAULT_COMPRESSION, header.getCompressionLevel());
		//
		builder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL);
		header = builder.build();
		assertTrue(header.hasFlag(GeneralPurposeFlag.DEFLATING_NORMAL));
		assertEquals(Deflater.DEFAULT_COMPRESSION, header.getCompressionLevel());
		//
		builder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_MAXIMUM);
		header = builder.build();
		assertTrue(header.hasFlag(GeneralPurposeFlag.DEFLATING_MAXIMUM));
		assertEquals(Deflater.BEST_COMPRESSION, header.getCompressionLevel());
		//
		builder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_SUPER_FAST);
		header = builder.build();
		assertTrue(header.hasFlag(GeneralPurposeFlag.DEFLATING_SUPER_FAST));
		assertEquals(Deflater.BEST_SPEED, header.getCompressionLevel());
	}

	@Test
	public void testReadWrongMagic() throws IOException {
		assertNull(ZipFileHeader
				.read(new RewindableInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6 }), 1024)));
	}

	@Test
	public void testToStringEmpty() {
		assertNotNull(ZipFileHeader.builder().build().toString());
	}

	@Test
	public void testNeedsDataDescriptor() {
		Builder builder = ZipFileHeader.builder();
		assertTrue(builder.build().needsDataDescriptor());
		builder.setCompressedSize(1);
		assertTrue(builder.build().needsDataDescriptor());
		builder.setCrc32(2);
		assertFalse(builder.build().needsDataDescriptor());
	}

	@Test
	public void testSetCrcFromValue() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		CRC32 crc32 = new CRC32();
		builder.setCrc32Value(crc32);
	}

	@Test
	public void testDataDescriptor() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipEntry entry = new ZipEntry("hello");
		ZipOutputStream zos = new ZipOutputStream(baos);
		zos.putNextEntry(entry);
		byte[] bytes1 = new byte[] { 1, 2, 3 };
		zos.write(bytes1);
		zos.closeEntry();
		zos.close();

		InputStream input = new ByteArrayInputStream(baos.toByteArray());
		ZipFileHeader header = ZipFileHeader.read(new RewindableInputStream(input, 8192));
		assertEquals(entry.getName(), header.getFileName());
		assertEquals(0, header.getCompressedSize());
		assertEquals(0, header.getUncompressedSize());
		assertTrue(header.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR));
	}

	@Test
	public void testLargeSizes() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setCompressedSize(ZipFileHeader.MAX_4_BYTE_SIZE);
		assertNull(builder.build().getZip64ExtraField());
		builder.setCompressedSize(ZipFileHeader.MAX_4_BYTE_SIZE + 1);
		assertNotNull(builder.build().getZip64ExtraField());

		builder.setCompressedSize(ZipFileHeader.MAX_4_BYTE_SIZE);
		builder.setUncompressedSize(ZipFileHeader.MAX_4_BYTE_SIZE + 1);
		assertNotNull(builder.build().getZip64ExtraField());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddExtraFieldZip64Id() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		UnknownExtraField extraField = new UnknownExtraField(Zip64ExtraField.EXPECTED_ID, new byte[0]);
		builder.addExtraField(extraField);
	}

	@Test
	public void testAddExtraField() throws IOException {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		UnknownExtraField extraField1 = new UnknownExtraField(213213, new byte[0]);
		builder.addExtraField(extraField1);
		ZipFileHeader header = builder.build();
		assertArrayEquals(extraField1.getExtraFieldBytes(), header.getExtraFieldBytes());
		assertEquals(extraField1.getExtraSize(), extraField1.getBytes().length);
		UnknownExtraField extraField2 = new UnknownExtraField(334534, new byte[] { 3, 4, 1, 2 });
		builder.addExtraField(extraField2);
		header = builder.build();
		assertArrayEquals(appendBytes(extraField1.getExtraFieldBytes(), extraField2.getExtraFieldBytes()),
				header.getExtraFieldBytes());
		assertEquals(extraField1.getExtraSize(), extraField1.getBytes().length);
		assertEquals(extraField2.getExtraSize(), extraField2.getBytes().length);
	}

	@Test
	public void testAddZip64Extra() throws IOException {
		long uncompressedSize = 12312321321312312L;
		long compressedSize = 5675676555445345L;
		long offset = 7334545345435345435L;
		int diskNumber = 12;
		Zip64ExtraField zip1 = new Zip64ExtraField(uncompressedSize, compressedSize, offset, diskNumber);
		assertEquals(uncompressedSize, zip1.getUncompressedSize());
		assertEquals(compressedSize, zip1.getCompressedSize());
		assertEquals(offset, zip1.getOffset());
		assertEquals(diskNumber, zip1.getDiskNumber());

		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setZip64ExtraField(zip1);
		// coverage
		builder.addExtraField(zip1);
		assertSame(zip1, builder.getZip64ExtraField());

		ZipFileHeader header = builder.build();
		assertEquals(uncompressedSize, header.getZip64UncompressedSize());
		assertEquals(compressedSize, header.getZip64CompressedSize());
		assertArrayEquals(header.getExtraFieldBytes(), zip1.getExtraFieldBytes());

		// coverage
		UnknownExtraField field = new UnknownExtraField(1313213, new byte[] { 3, 4, 1, 2 });
		builder.setExtraFieldBytes(field.getExtraFieldBytes());

		header = builder.build();
		assertArrayEquals(appendBytes(zip1.getExtraFieldBytes(), field.getExtraFieldBytes()),
				header.getExtraFieldBytes());
	}

	@Test
	public void testAddZip64ExtraAsBytes() {
		long uncompressedSize = 12321321312312L;
		long compressedSize = 567565445345L;
		long offset = 73345435345435L;
		int diskNumber = 13242;
		Zip64ExtraField zip = new Zip64ExtraField(uncompressedSize, compressedSize, offset, diskNumber);

		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setExtraFieldBytes(zip.getExtraFieldBytes());
		// coverage
		builder.addExtraField(zip);
		assertSame(zip, builder.getZip64ExtraField());

		ZipFileHeader header = builder.build();
		assertEquals(uncompressedSize, header.getZip64UncompressedSize());
		assertEquals(compressedSize, header.getZip64CompressedSize());
	}

	@Test
	public void testAddUnknownExtraAsBytes() throws IOException {
		UnknownExtraField field = new UnknownExtraField(1313213, new byte[] { 3, 4, 1, 2 });

		ZipFileHeader.Builder builder = ZipFileHeader.builder();
		builder.setExtraFieldBytes(field.getExtraFieldBytes());
		// coverage
		builder.addExtraField(field);
		assertNull(builder.getZip64ExtraField());
		assertArrayEquals(field.getExtraFieldBytes(), builder.getExtraFieldBytes());

		ZipFileHeader header = builder.build();
		assertNull(header.getZip64ExtraField());
		assertArrayEquals(appendBytes(field.getExtraFieldBytes(), field.getExtraFieldBytes()),
				header.getExtraFieldBytes());
	}

	private byte[] appendBytes(byte[]... byteArrays) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte[] byteArray : byteArrays) {
			baos.write(byteArray);
		}
		return baos.toByteArray();
	}
}
