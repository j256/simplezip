package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.junit.Test;

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
		int date = builder.getLastModifiedFileDate();
		int time = builder.getLastModifiedFileTime();
		ZipFileHeader header = builder.build();

		LocalDateTime output = header.getLastModFileDateTime();
		assertEquals(input, output);
		assertEquals(date, header.getLastModifiedFileDate());
		assertEquals(time, header.getLastModifiedFileTime());
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
		CompressionMethod compressionMethodEnum = CompressionMethod.DEFLATED;
		builder.setCompressionMethod(compressionMethodEnum);
		assertEquals(compressionMethodEnum, builder.getCompressionMethodAsEnum());
		int compressionMethod = 6334324;
		builder.setCompressionMethod(compressionMethod);
		assertEquals(compressionMethod, builder.getCompressionMethod());
		int lastModifiedFileTime = 322342434;
		builder.setLastModifiedFileTime(lastModifiedFileTime);
		assertEquals(lastModifiedFileTime, builder.getLastModifiedFileTime());
		int lastModifiedFileDate = 8567056;
		builder.setLastModifiedFileDate(lastModifiedFileDate);
		assertEquals(lastModifiedFileDate, builder.getLastModifiedFileDate());
		int crc32 = 654654;
		builder.setCrc32(crc32);
		assertEquals(crc32, builder.getCrc32());
		int compressedSize = 42343423;
		builder.setCompressedSize(compressedSize);
		assertEquals(compressedSize, builder.getCompressedSize());
		int uncompressedSize = 65654568;
		builder.setUncompressedSize(uncompressedSize);
		assertEquals(uncompressedSize, builder.getUncompressedSize());
		byte[] fileNameBytes = new byte[] { 4, 5, 1, 1, 3, 5 };
		builder.setFileNameBytes(fileNameBytes);
		assertArrayEquals(fileNameBytes, builder.getFileNameBytes());
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.setExtraFieldBytes(extraBytes);
		assertEquals(extraBytes, builder.getExtraFieldBytes());

		ZipFileHeader fileHeader = builder.build();
		assertEquals(versionNeeded, fileHeader.getVersionNeeded());
		assertEquals(generalPurposeFlags, fileHeader.getGeneralPurposeFlags());
		assertEquals(compressionMethod, fileHeader.getCompressionMethod());
		assertEquals(lastModifiedFileTime, fileHeader.getLastModifiedFileTime());
		assertEquals(lastModifiedFileDate, fileHeader.getLastModifiedFileDate());
		assertEquals(crc32, fileHeader.getCrc32());
		assertEquals(compressedSize, fileHeader.getCompressedSize());
		assertEquals(uncompressedSize, fileHeader.getUncompressedSize());
		assertArrayEquals(fileNameBytes, fileHeader.getFileNameBytes());
		assertArrayEquals(extraBytes, fileHeader.getExtraFieldBytes());

		System.out.println("Date-time is: " + fileHeader.getLastModifiedFileDateString());
		System.out.println("to-string is: " + fileHeader);
	}

	@Test
	public void testFlags() {
		ZipFileHeader.Builder builder = ZipFileHeader.builder();

		int flags = 0;
		builder.addGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR);
		flags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

		builder.addGeneralPurposeFlags(GeneralPurposeFlag.DEFLATING_NORMAL, GeneralPurposeFlag.LANGUAGE_ENCODING);
		flags |= GeneralPurposeFlag.DEFLATING_NORMAL.getValue() | GeneralPurposeFlag.LANGUAGE_ENCODING.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

		builder.addGeneralPurposeFlags(Arrays.asList(GeneralPurposeFlag.ENCRYPTED));
		flags |= GeneralPurposeFlag.ENCRYPTED.getValue();
		assertEquals(flags, builder.getGeneralPurposeFlags());

	}
}
