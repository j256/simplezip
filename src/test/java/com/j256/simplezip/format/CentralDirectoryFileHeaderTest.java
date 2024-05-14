package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.simplezip.format.CentralDirectoryFileHeader.Builder;

public class CentralDirectoryFileHeaderTest {

	@Test
	public void testCoverage() {
		Builder builder = CentralDirectoryFileHeader.builder();

		int versionMade = 1312;
		builder.setVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		int versionNeeded = 5251312;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		int generalPurposeFlags = 565479567;
		builder.setGeneralPurposeFlags(generalPurposeFlags);
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		int compressionMethodValue = 6334324;
		builder.setCompressionMethod(compressionMethodValue);
		assertEquals(compressionMethodValue, builder.getCompressionMethod());
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
		byte[] fileNameBytes = new byte[] { 4, 5, 1, 1, 3, 5 };
		builder.setFileNameBytes(fileNameBytes);
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
		internalFileAttributes |= CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE;

		CentralDirectoryFileHeader fileHeader = builder.build();
		assertEquals(versionMade, fileHeader.getVersionMade());
		assertEquals(versionNeeded, fileHeader.getVersionNeeded());
		assertEquals(generalPurposeFlags, fileHeader.getGeneralPurposeFlags());
		assertEquals(compressionMethodValue, fileHeader.getCompressionMethod());
		assertEquals(lastModifiedFileTime, fileHeader.getLastModifiedFileTime());
		assertEquals(lastModifiedFileDate, fileHeader.getLastModifiedFileDate());
		assertEquals(crc32, fileHeader.getCrc32());
		assertEquals(compressedSize, fileHeader.getCompressedSize());
		assertEquals(uncompressedSize, fileHeader.getUncompressedSize());
		assertEquals(diskNumberStart, fileHeader.getDiskNumberStart());
		assertEquals(internalFileAttributes, fileHeader.getInternalFileAttributes());
		assertEquals(externalFileAttributes, fileHeader.getExternalFileAttributes());
		assertEquals(relativeOffsetOfLocalHeader, fileHeader.getRelativeOffsetOfLocalHeader());
		assertArrayEquals(fileNameBytes, fileHeader.getFileNameBytes());
		assertArrayEquals(extraBytes, fileHeader.getExtraFieldBytes());
		assertArrayEquals(commentBytes, fileHeader.getCommentBytes());
		assertEquals(new String(commentBytes), fileHeader.getComment());
		assertEquals(textFile, fileHeader.isTextFile());
	}

	@Test
	public void testPlatformAndVersion() {
		Builder builder = CentralDirectoryFileHeader.builder();

		Platform platform = Platform.UNIX;
		builder.setPlatform(platform);
		assertEquals(platform, builder.getPlatform());
		ZipVersion version = ZipVersion.V4_5;
		builder.setZipVersion(version);
		assertEquals(version, builder.getZipVersion());

		CentralDirectoryFileHeader fileHeader = builder.build();
		assertEquals(platform, fileHeader.getPlatform());
		assertEquals(version, fileHeader.getZipVersion());
	}

	@Test
	public void testMethod() {
		Builder builder = CentralDirectoryFileHeader.builder();

		CompressionMethod method = CompressionMethod.DEFLATED;
		builder.setCompressionMethod(method);
		assertEquals(method, builder.getCompressionMethodAsEnum());

		CentralDirectoryFileHeader fileHeader = builder.build();
		assertEquals(method, fileHeader.getCompressionMethodAsEnum());
	}

	@Test
	public void testAddFileInfo() {

		CentralDirectoryFileInfo.Builder fileInfoBuilder = CentralDirectoryFileInfo.builder();
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
		CentralDirectoryFileInfo fileInfo = fileInfoBuilder.build();

		Builder builder = CentralDirectoryFileHeader.builder();
		builder.addFileInfo(fileInfo);

		CentralDirectoryFileHeader fileHeader = builder.build();
		assertEquals(versionMade, fileHeader.getVersionMade());
		assertEquals(versionNeeded, fileHeader.getVersionNeeded());
		assertEquals(diskNumberStart, fileHeader.getDiskNumberStart());
		assertEquals(internalFileAttributes, fileHeader.getInternalFileAttributes());
		assertEquals(externalFileAttributes, fileHeader.getExternalFileAttributes());
		assertArrayEquals(commentBytes, fileHeader.getCommentBytes());
	}

	@Test
	public void testFromFileHeader() {
		Builder builder = CentralDirectoryFileHeader.builder();

		int versionMade = 1312;
		builder.setVersionMade(versionMade);
		int versionNeeded = 5251312;
		builder.setVersionNeeded(versionNeeded);
		int generalPurposeFlags = 565479567;
		builder.setGeneralPurposeFlags(generalPurposeFlags);
		int compressionMethodValue = 6334324;
		builder.setCompressionMethod(compressionMethodValue);
		int lastModifiedFileTime = 322342434;
		builder.setLastModifiedFileTime(lastModifiedFileTime);
		int lastModifiedFileDate = 8567056;
		builder.setLastModifiedFileDate(lastModifiedFileDate);
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
		internalFileAttributes |= CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE;

		CentralDirectoryFileHeader fileHeader = builder.build();
		builder = Builder.fromFileHeader(fileHeader);
		assertEquals(versionMade, builder.getVersionMade());
		assertEquals(versionNeeded, builder.getVersionNeeded());
		assertEquals(generalPurposeFlags, builder.getGeneralPurposeFlags());
		assertEquals(compressionMethodValue, builder.getCompressionMethod());
		assertEquals(lastModifiedFileTime, builder.getLastModifiedFileTime());
		assertEquals(lastModifiedFileDate, builder.getLastModifiedFileDate());
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
}
