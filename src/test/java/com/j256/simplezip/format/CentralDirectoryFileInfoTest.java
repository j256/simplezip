package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CentralDirectoryFileInfoTest {

	@Test
	public void testCoverage() {
		CentralDirectoryFileInfo.Builder builder = CentralDirectoryFileInfo.builder();

		int versionMade = 1312;
		builder.setVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		int versionNeeded = 76756;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		int diskNumberStart = 87696796;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int internalFileAttributes = 343456457;
		builder.setInternalFileAttributes(internalFileAttributes);
		assertEquals(internalFileAttributes, builder.getInternalFileAttributes());
		int externalFileAttributes = 56357634;
		builder.setExternalFileAttributes(externalFileAttributes);
		assertEquals(externalFileAttributes, builder.getExternalFileAttributes());
		String comment = "fewjpfjewp";
		byte[] commentBytes = comment.getBytes();
		builder.setComment(comment);
		assertEquals(comment, builder.getComment());
		builder.setCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());
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

		CentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(versionMade, fileInfo.getVersionMade());
		assertEquals(versionNeeded, fileInfo.getVersionNeeded());
		assertEquals(diskNumberStart, fileInfo.getDiskNumberStart());
		assertEquals(internalFileAttributes, fileInfo.getInternalFileAttributes());
		assertEquals(externalFileAttributes, fileInfo.getExternalFileAttributes());
		assertEquals(comment, fileInfo.getComment());
		assertEquals(commentBytes, fileInfo.getCommentBytes());
		assertEquals(textFile, fileInfo.isTextFile());
	}

	@Test
	public void testNotText() {
		CentralDirectoryFileInfo.Builder builder = CentralDirectoryFileInfo.builder();
		boolean textFile = false;
		builder.setTextFile(textFile);
		CentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(textFile, fileInfo.isTextFile());
	}

	@Test
	public void testFromFileHeader() {
		CentralDirectoryFileHeader.Builder headerBuilder = CentralDirectoryFileHeader.builder();

		int versionMade = 1312;
		headerBuilder.setVersionMade(versionMade);
		int versionNeeded = 5251312;
		headerBuilder.setVersionNeeded(versionNeeded);
		int diskNumberStart = 5664572;
		headerBuilder.setDiskNumberStart(diskNumberStart);
		int internalFileAttributes = 6554634;
		headerBuilder.setInternalFileAttributes(internalFileAttributes);
		int externalFileAttributes = 976753523;
		headerBuilder.setExternalFileAttributes(externalFileAttributes);
		String comment = "comment 1 2 3";
		byte[] commentBytes = comment.getBytes();
		headerBuilder.setCommentBytes(commentBytes);
		boolean textFile = true;
		headerBuilder.setTextFile(textFile);
		// NOTE: this changes the internal file attributes
		internalFileAttributes |= CentralDirectoryFileHeader.INTERNAL_ATTRIBUTES_TEXT_FILE;

		CentralDirectoryFileHeader fileHeader = headerBuilder.build();

		CentralDirectoryFileInfo.Builder builder = CentralDirectoryFileInfo.Builder.fromFileHeader(fileHeader);
		assertEquals(versionMade, builder.getVersionMade());
		assertEquals(versionNeeded, builder.getVersionNeeded());
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		assertEquals(internalFileAttributes, builder.getInternalFileAttributes());
		assertEquals(externalFileAttributes, builder.getExternalFileAttributes());
		assertEquals(comment, builder.getComment());
		assertEquals(commentBytes, builder.getCommentBytes());
		assertEquals(textFile, builder.isTextFile());
	}

	@Test
	public void testPlatformAndVersion() {
		CentralDirectoryFileInfo.Builder builder = CentralDirectoryFileInfo.builder();

		Platform platform = Platform.UNIX;
		builder.setPlatform(platform);
		assertEquals(platform, builder.getPlatform());
		ZipVersion version = ZipVersion.V4_5;
		builder.setZipVersion(version);
		assertEquals(version, builder.getZipVersion());

		CentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(platform, fileInfo.getPlatform());
		assertEquals(version, fileInfo.getZipVersion());
	}
}
