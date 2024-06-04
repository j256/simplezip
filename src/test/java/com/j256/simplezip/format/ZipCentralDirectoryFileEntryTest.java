package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.j256.simplezip.format.ZipCentralDirectoryFileEntry.Builder;
import com.j256.simplezip.format.extra.UnknownExtraField;
import com.j256.simplezip.format.extra.Zip64ExtraField;

public class ZipCentralDirectoryFileEntryTest {

	@Test
	public void testCoverage() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		int major = 2;
		int minor = 4;
		builder.setVersionMadeMajorMinor(major, minor);
		assertEquals((major * 10 + minor), builder.getVersionMadeMajorMinor());
		int versionMade = 131;
		builder.setVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		major = 3;
		minor = 8;
		builder.setVersionNeededMajorMinor(major, minor);
		assertEquals((major * 10 + minor), builder.getVersionNeeded());
		int versionNeeded = 212;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		List<GeneralPurposeFlag> generalPurposeFlagList =
				Arrays.asList(GeneralPurposeFlag.DEFLATING_MAXIMUM, GeneralPurposeFlag.ENCRYPTED);
		Collections.sort(generalPurposeFlagList);
		int generalPurposeFlags = 0;
		for (GeneralPurposeFlag flag : generalPurposeFlagList) {
			generalPurposeFlags |= flag.getValue();
		}
		builder.setGeneralPurposeFlags(generalPurposeFlags);
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		int compressionMethodValue = 6334324;
		builder.setCompressionMethod(compressionMethodValue);
		assertEquals(compressionMethodValue, builder.getCompressionMethod());
		int lastModifiedFileTime = 32434;
		builder.setLastModifiedTime(lastModifiedFileTime);
		assertEquals(lastModifiedFileTime, builder.getLastModifiedTime());
		int lastModifiedFileDate = 1267556;
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
		int diskNumberStart = 5664572;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int internalFileAttributes = 6554634;
		builder.setInternalFileAttributes(internalFileAttributes);
		assertEquals(internalFileAttributes, builder.getInternalFileAttributes());
		int externalFileAttributes = 976753523;
		builder.setExternalFileAttributes(externalFileAttributes);
		assertEquals(externalFileAttributes, builder.getExternalFileAttributes());
		int relativeOffsetOfLocalHeader = 663523534;
		builder.setRelativeOffsetOfLocalHeader(relativeOffsetOfLocalHeader);
		assertEquals(relativeOffsetOfLocalHeader, builder.getRelativeOffsetOfLocalHeader());
		assertNull(builder.getFileName());
		String fileName = "hello.txt";
		byte[] fileNameBytes = fileName.getBytes();
		builder.setFileNameBytes(fileNameBytes);
		assertEquals(fileName, builder.getFileName());
		assertArrayEquals(fileNameBytes, builder.getFileNameBytes());
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.setExtraFieldBytes(extraBytes);
		assertEquals(extraBytes, builder.getExtraFieldBytes());
		byte[] commentBytes = new String("comment").getBytes();
		builder.setCommentBytes(commentBytes);
		assertEquals(commentBytes, builder.getCommentBytes());
		boolean textFile = true;
		builder.setTextFile(textFile);
		assertEquals(textFile, builder.isTextFile());
		textFile = false;
		builder.setTextFile(textFile);
		assertEquals(textFile, builder.isTextFile());
		textFile = true;
		builder.setTextFile(textFile);
		// NOTE: this changes the internal file attributes
		internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		assertEquals(versionMade, fileEntry.getVersionMade());
		assertEquals((versionMade & 0XFF), fileEntry.getVersionMadeMajorMinor());
		assertEquals(versionNeeded, fileEntry.getVersionNeeded());
		assertEquals("21.2", fileEntry.getVersionNeededMajorMinorString());
		List<GeneralPurposeFlag> resultList = new ArrayList<>(fileEntry.getGeneralPurposeFlagsAsEnums());
		Collections.sort(resultList);
		assertEquals(generalPurposeFlagList, resultList);
		assertEquals(generalPurposeFlags, fileEntry.getGeneralPurposeFlags());
		assertEquals(compressionMethodValue, fileEntry.getCompressionMethod());
		assertEquals(lastModifiedFileTime, fileEntry.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, fileEntry.getLastModifiedDate());
		assertEquals(crc32, fileEntry.getCrc32());
		assertEquals(compressedSize, fileEntry.getCompressedSize());
		assertEquals(compressedSize, fileEntry.getZip64CompressedSize());
		assertEquals(uncompressedSize, fileEntry.getUncompressedSize());
		assertEquals(uncompressedSize, fileEntry.getZip64UncompressedSize());
		assertEquals(diskNumberStart, fileEntry.getDiskNumberStart());
		assertEquals(internalFileAttributes, fileEntry.getInternalFileAttributes());
		assertEquals(externalFileAttributes, fileEntry.getExternalFileAttributes());
		assertEquals(relativeOffsetOfLocalHeader, fileEntry.getRelativeOffsetOfLocalHeader());
		assertArrayEquals(fileNameBytes, fileEntry.getFileNameBytes());
		assertArrayEquals(extraBytes, fileEntry.getExtraFieldBytes());
		assertArrayEquals(commentBytes, fileEntry.getCommentBytes());
		assertEquals(new String(commentBytes), fileEntry.getComment());
		assertEquals(textFile, fileEntry.isTextFile());

		System.out.println("entry = " + fileEntry);
	}

	@Test
	public void testGeneralPurposeFlags() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		builder.assignGeneralPurposeFlag(GeneralPurposeFlag.DEFLATING_FAST, false);
		assertTrue((builder.getGeneralPurposeFlags() & GeneralPurposeFlag.DEFLATING_FAST.getValue()) == 0);
		builder.assignGeneralPurposeFlag(GeneralPurposeFlag.DEFLATING_FAST, true);
		assertTrue((builder.getGeneralPurposeFlags() & GeneralPurposeFlag.DEFLATING_FAST.getValue()) != 0);

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		assertTrue((fileEntry.getGeneralPurposeFlags() & GeneralPurposeFlag.DEFLATING_FAST.getValue()) != 0);
	}

	@Test
	public void testComment() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		String comment = "wow!";
		builder.setComment(comment);
		assertEquals(comment, builder.getComment());
		ZipCentralDirectoryFileEntry entry = builder.build();
		assertEquals(comment, entry.getComment());

		builder.setComment(null);
		assertNull(builder.getComment());
		entry = builder.build();
		assertNull(entry.getComment());
	}

	@Test
	public void testInternalText() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		builder.setTextFile(false);
		assertFalse(builder.isTextFile());
		ZipCentralDirectoryFileEntry entry = builder.build();
		assertFalse(entry.isTextFile());

		builder.setTextFile(true);
		assertTrue(builder.isTextFile());
		entry = builder.build();
		assertTrue(entry.isTextFile());

		System.out.println("no file: " + entry);
	}

	@Test
	public void testDateTime() {
		LocalDateTime input;
		do {
			input = LocalDateTime.now();
			input = input.truncatedTo(ChronoUnit.SECONDS);
		} while (input.getSecond() % 2 != 0);

		Builder builder = ZipCentralDirectoryFileEntry.builder();
		builder.setLastModifiedDateTime(input);
		int date = builder.getLastModifiedDate();
		int time = builder.getLastModifiedTime();
		// coverage
		builder.withLastModifiedDateTime(input);
		ZipCentralDirectoryFileEntry entry = builder.build();
		System.out.println("last-mod date is: " + entry.getLastModifiedDateString());
		System.out.println("last-mod time is: " + entry.getLastModifiedTimeString());

		LocalDateTime output = entry.getLastModifiedDateTime();
		System.out.println("last-mod date is: " + entry.getLastModifiedDate());
		System.out.println("last-mod time is: " + entry.getLastModifiedTime());
		assertEquals(input, output);
		assertEquals(date, entry.getLastModifiedDate());
		assertEquals(time, entry.getLastModifiedTime());
	}

	@Test
	public void testPlatformAndVersion() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		Platform platform = Platform.UNIX;
		builder.setPlatformMade(platform);
		assertEquals(platform, builder.getPlatformMade());
		int major = 4;
		int minor = 5;
		builder.setVersionMadeMajorMinor(4, 5);
		assertEquals(major * 10 + minor, builder.getVersionMadeMajorMinor());

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		assertEquals(platform, fileEntry.getPlatformMade());
		assertEquals(major + "." + minor, fileEntry.getVersionMadeMajorMinorString());
	}

	@Test
	public void testMethod() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		CompressionMethod method = CompressionMethod.DEFLATED;
		builder.setCompressionMethod(method);
		assertEquals(method, builder.getCompressionMethodAsEnum());

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		assertEquals(method, fileEntry.getCompressionMethodAsEnum());
	}

	@Test
	public void testAddFileInfo() {

		ZipCentralDirectoryFileInfo.Builder fileInfoBuilder = ZipCentralDirectoryFileInfo.builder();
		int versionMade = 345534534;
		fileInfoBuilder.setVersionMade(versionMade);
		assertEquals(versionMade, fileInfoBuilder.getVersionMade());
		int versionNeeded = 765756765;
		fileInfoBuilder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, fileInfoBuilder.getVersionNeeded());
		int diskNumberStart = 3654252;
		fileInfoBuilder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, fileInfoBuilder.getDiskNumberStart());
		int internalFileAttributes = 435345346;
		fileInfoBuilder.setInternalFileAttributes(internalFileAttributes);
		assertEquals(internalFileAttributes, fileInfoBuilder.getInternalFileAttributes());
		int externalFileAttributes = 532234423;
		fileInfoBuilder.setExternalFileAttributes(externalFileAttributes);
		assertEquals(externalFileAttributes, fileInfoBuilder.getExternalFileAttributes());
		byte[] commentBytes = "zipper".getBytes();
		fileInfoBuilder.setCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, fileInfoBuilder.getCommentBytes());
		ZipCentralDirectoryFileInfo fileInfo = fileInfoBuilder.build();

		Builder builder = ZipCentralDirectoryFileEntry.builder();
		builder.addFileInfo(fileInfo);

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		assertEquals(versionMade, fileEntry.getVersionMade());
		assertEquals(versionNeeded, fileEntry.getVersionNeeded());
		assertEquals(diskNumberStart, fileEntry.getDiskNumberStart());
		assertEquals(internalFileAttributes, fileEntry.getInternalFileAttributes());
		assertEquals(externalFileAttributes, fileEntry.getExternalFileAttributes());
		assertArrayEquals(commentBytes, fileEntry.getCommentBytes());
	}

	@Test
	public void testNoFileName() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();
		ZipCentralDirectoryFileEntry entry = builder.build();
		assertNull(entry.getFileName());
		assertNull(entry.getFileNameBytes());
	}

	@Test
	public void testFromFileHeader() {
		Builder builder = ZipCentralDirectoryFileEntry.builder();

		int versionMade = 1312;
		builder.setVersionMade(versionMade);
		int versionNeeded = 5251312;
		builder.setVersionNeeded(versionNeeded);
		int generalPurposeFlags = 565479567;
		builder.setGeneralPurposeFlags(generalPurposeFlags);
		int compressionMethodValue = 6334324;
		builder.setCompressionMethod(compressionMethodValue);
		int lastModifiedFileTime = 322342434;
		builder.setLastModifiedTime(lastModifiedFileTime);
		int lastModifiedFileDate = 8567056;
		builder.setLastModifiedDate(lastModifiedFileDate);
		int crc32 = 654654;
		builder.setCrc32(crc32);
		int compressedSize = 42343423;
		builder.setCompressedSize(compressedSize);
		int uncompressedSize = 65654568;
		builder.setUncompressedSize(uncompressedSize);
		int diskNumberStart = 5664572;
		builder.setDiskNumberStart(diskNumberStart);
		int internalFileAttributes = 6554634;
		builder.setInternalFileAttributes(internalFileAttributes);
		int externalFileAttributes = 976753523;
		builder.setExternalFileAttributes(externalFileAttributes);
		int relativeOffsetOfLocalHeader = 663523534;
		builder.setRelativeOffsetOfLocalHeader(relativeOffsetOfLocalHeader);
		byte[] fileNameBytes = new byte[] { 4, 5, 1, 1, 3, 5 };
		builder.setFileNameBytes(fileNameBytes);
		byte[] extraBytes = new byte[] { 7, 8, 1, 2, 1, 5 };
		builder.setExtraFieldBytes(extraBytes);
		byte[] commentBytes = new String("comment").getBytes();
		builder.setCommentBytes(commentBytes);
		boolean textFile = true;
		builder.setTextFile(textFile);
		internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;

		ZipCentralDirectoryFileEntry fileEntry = builder.build();
		builder = Builder.fromFileHeader(fileEntry);
		assertEquals(versionMade, builder.getVersionMade());
		assertEquals(versionNeeded, builder.getVersionNeeded());
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		assertEquals(compressionMethodValue, builder.getCompressionMethod());
		assertEquals(lastModifiedFileTime, builder.getLastModifiedTime());
		assertEquals(lastModifiedFileDate, builder.getLastModifiedDate());
		assertEquals(crc32, builder.getCrc32());
		assertEquals(compressedSize, builder.getCompressedSize());
		assertEquals(uncompressedSize, builder.getUncompressedSize());
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		assertEquals(internalFileAttributes, builder.getInternalFileAttributes());
		assertEquals(externalFileAttributes, builder.getExternalFileAttributes());
		assertEquals(relativeOffsetOfLocalHeader, builder.getRelativeOffsetOfLocalHeader());
		assertArrayEquals(fileNameBytes, builder.getFileNameBytes());
		assertArrayEquals(extraBytes, builder.getExtraFieldBytes());
		assertArrayEquals(commentBytes, builder.getCommentBytes());
		assertEquals(commentBytes, builder.getCommentBytes());
		assertEquals(new String(commentBytes), builder.getComment());
		assertEquals(textFile, builder.isTextFile());
	}

	@Test
	public void testLargeSizes() {
		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
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
		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
		UnknownExtraField extraField = new UnknownExtraField(Zip64ExtraField.EXPECTED_ID, new byte[0]);
		builder.addExtraField(extraField);
	}

	@Test
	public void testAddExtraField() throws IOException {
		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
		UnknownExtraField extraField1 = new UnknownExtraField(213213, new byte[0]);
		builder.addExtraField(extraField1);
		ZipCentralDirectoryFileEntry header = builder.build();
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

		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
		builder.setZip64ExtraField(zip1);
		// coverage
		builder.addExtraField(zip1);
		assertSame(zip1, builder.getZip64ExtraField());

		ZipCentralDirectoryFileEntry header = builder.build();
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

		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
		builder.setExtraFieldBytes(zip.getExtraFieldBytes());
		// coverage
		builder.addExtraField(zip);
		assertSame(zip, builder.getZip64ExtraField());

		ZipCentralDirectoryFileEntry header = builder.build();
		assertEquals(uncompressedSize, header.getZip64UncompressedSize());
		assertEquals(compressedSize, header.getZip64CompressedSize());
	}

	@Test
	public void testAddUnknownExtraAsBytes() throws IOException {
		UnknownExtraField field = new UnknownExtraField(1313213, new byte[] { 3, 4, 1, 2 });

		ZipCentralDirectoryFileEntry.Builder builder = ZipCentralDirectoryFileEntry.builder();
		builder.setExtraFieldBytes(field.getExtraFieldBytes());
		// coverage
		builder.addExtraField(field);
		assertNull(builder.getZip64ExtraField());
		assertArrayEquals(field.getExtraFieldBytes(), builder.getExtraFieldBytes());

		ZipCentralDirectoryFileEntry header = builder.build();
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
