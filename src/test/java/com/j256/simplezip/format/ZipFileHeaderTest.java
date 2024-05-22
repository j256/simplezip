package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.ZipFileHeader.Builder;

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
		assertEquals(generalPurposeFlags, header.getGeneralPurposeFlags());
		assertEquals(lastModifiedFileTime, header.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, header.getLastModifiedDate());
		assertEquals(crc32, header.getCrc32());
		assertEquals(compressedSize, header.getCompressedSize());
		assertEquals(uncompressedSize, header.getUncompressedSize());
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
	public void testBuildWith() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();

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
				result.getGeneralPurposeFlagAsEnums());
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
		assertEquals(
				new HashSet<GeneralPurposeFlag>(
						Arrays.asList(GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.DATA_DESCRIPTOR)),
				result.getGeneralPurposeFlagAsEnums());
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
}
