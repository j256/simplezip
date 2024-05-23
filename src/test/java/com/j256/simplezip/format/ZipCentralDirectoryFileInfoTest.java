package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ZipCentralDirectoryFileInfoTest {

	@Test
	public void testCoverage() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

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
		builder.setComment(null);
		assertNull(builder.getComment());
		assertNull(builder.getCommentBytes());
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
		internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;

		ZipCentralDirectoryFileInfo fileInfo = builder.build();
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
	public void testNoComment() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		builder.setComment(null);
		ZipCentralDirectoryFileInfo fileInfo = builder.build();
		assertNull(fileInfo.getComment());
		assertNull(fileInfo.getCommentBytes());
	}

	@Test
	public void testBuildWith() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

		int versionMade = 1312;
		builder.withVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		int versionNeeded = 76756;
		builder.withVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		int diskNumberStart = 87696796;
		builder.withDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int internalFileAttributes = 343456457;
		builder.withInternalFileAttributes(internalFileAttributes);
		assertEquals(internalFileAttributes, builder.getInternalFileAttributes());
		int externalFileAttributes = 56357634;
		builder.withExternalFileAttributes(externalFileAttributes);
		assertEquals(externalFileAttributes, builder.getExternalFileAttributes());
		assertNull(builder.getComment());
		String comment = "fewjpfjewp";
		byte[] commentBytes = comment.getBytes();
		builder.withComment(comment);
		assertEquals(comment, builder.getComment());
		builder.withCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());
		boolean textFile = true;
		builder.withTextFile(textFile);
		assertEquals(textFile, builder.isTextFile());
		textFile = false;
		builder.withTextFile(textFile);
		assertEquals(textFile, builder.isTextFile());
		textFile = true;
		builder.withTextFile(textFile);
		// NOTE: this changes the internal file attributes
		internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;

		ZipCentralDirectoryFileInfo fileInfo = builder.build();
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
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		boolean textFile = false;
		builder.setTextFile(textFile);
		ZipCentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(textFile, fileInfo.isTextFile());
	}

	@Test
	public void testFromFileHeader() {
		ZipCentralDirectoryFileEntry.Builder headerBuilder = ZipCentralDirectoryFileEntry.builder();

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
		internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;

		ZipCentralDirectoryFileEntry fileHeader = headerBuilder.build();

		ZipCentralDirectoryFileInfo.Builder builder =
				ZipCentralDirectoryFileInfo.Builder.fromCentralDirectoryFileEntry(fileHeader);
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
	public void testMadePlatformAndVersion() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

		Platform platform = Platform.UNIX;
		builder.setMadePlatform(platform);
		assertEquals(platform, builder.getMadePlatform());
		platform = Platform.MACINTOSH;
		builder.withMadePlatform(platform);
		assertEquals(platform, builder.getMadePlatform());
		ZipVersion version = ZipVersion.V4_5;
		builder.setMadeZipVersion(version);
		version = ZipVersion.V2_0;
		builder.withMadeZipVersion(version);
		assertEquals(version, builder.getMadeZipVersion());

		ZipCentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(platform, fileInfo.getMadePlatform());
		assertEquals(version, fileInfo.getMadeZipVersion());
	}

	@Test
	public void testNeededVersion() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

		ZipVersion version = ZipVersion.V4_6;
		builder.setNeededZipVersion(version);
		version = ZipVersion.V2_0;
		builder.withNeededZipVersion(version);
		assertEquals(version, builder.getNeededZipVersion());
		assertEquals(version.getValue(), builder.getVersionNeeded());

		ZipCentralDirectoryFileInfo fileInfo = builder.build();
		assertEquals(version, fileInfo.getNeededZipVersion());
		assertEquals(version.getValue(), fileInfo.getVersionNeeded());
		assertEquals("2.0", fileInfo.getVersionNeededString());
	}

	@Test
	public void testFileIsDirectory() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		assertEquals(0, builder.getExternalFileAttributes());
		builder.setFileIsDirectory(true);
		assertNotEquals(0, builder.getExternalFileAttributes());
		builder.withFileIsDirectory(false);
		assertEquals(0, builder.getExternalFileAttributes());
	}

	@Test
	public void testFileIsReadOnly() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		assertEquals(0, builder.getExternalFileAttributes());
		builder.setFileIsReadOnly(true);
		assertNotEquals(0, builder.getExternalFileAttributes());
		builder.withFileIsReadOnly(false);
		// adds in unix read-write permissions
		assertNotEquals(0, builder.getExternalFileAttributes());
	}

	@Test
	public void testFileIsSymlink() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		assertEquals(0, builder.getExternalFileAttributes());
		builder.setFileIsSymlink(true);
		assertNotEquals(0, builder.getExternalFileAttributes());
		builder.withFileIsSymlink(false);
		assertEquals(0, builder.getExternalFileAttributes());
	}

	@Test
	public void testFileIsRegular() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		assertEquals(0, builder.getExternalFileAttributes());
		builder.setFileIsRegular(true);
		assertNotEquals(0, builder.getExternalFileAttributes());
		builder.withFileIsRegular(false);
		assertEquals(0, builder.getExternalFileAttributes());
	}

	@Test
	public void testSetMsDosAttrs() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		assertEquals(0, builder.getExternalFileAttributes());
		int attrs = 10;
		builder.setMsDosExternalFileAttributes(attrs);
		assertEquals(attrs, builder.getExternalFileAttributes());
		attrs |= 10000000;
		builder.withMsDosExternalFileAttributes(attrs);
		assertNotEquals(attrs, builder.getExternalFileAttributes());
	}

	@Test
	public void testSetExternalFromFile() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();

		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

		file.setReadable(true);
		file.setExecutable(false);
		file.setWritable(false);
		builder.setExternalAttributesFromFile(file);
		assertTrue((0100444 << 16) == builder.getExternalFileAttributes()
				|| (0100440 << 16) == builder.getExternalFileAttributes());

		file.setExecutable(false);
		file.setWritable(true);
		builder.withExternalAttributesFromFile(file);
		assertTrue((0100644 << 16) == builder.getExternalFileAttributes()
				|| (0100640 << 16) == builder.getExternalFileAttributes());

		file.setExecutable(true);
		file.setWritable(true);
		builder.setExternalAttributesFromFile(file);
		// NOTE: 744 is what we get from posix
		assertTrue((0100744 << 16) == builder.getExternalFileAttributes()
				|| (0100740 << 16) == builder.getExternalFileAttributes());

		file.delete();
	}

	@Test
	public void testSetUnixExternalAttrs() {
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();

		int mode = 0644;
		builder.setUnixExternalFileAttributes(mode);
		assertEquals((mode << 16), builder.getExternalFileAttributes());

		mode = 0755;
		builder.withUnixExternalFileAttributes(mode);
		assertEquals((mode << 16), builder.getExternalFileAttributes());
	}
}
