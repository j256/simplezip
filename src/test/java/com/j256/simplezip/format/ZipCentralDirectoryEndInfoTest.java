package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplezip.IoUtils;

public class ZipCentralDirectoryEndInfoTest {

	@Test
	public void testCoverage() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();

		boolean zip64 = true;
		builder.setZip64(zip64);
		assertEquals(zip64, builder.isZip64());
		int versionMade = 2354;
		builder.setVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		int versionNeeded = 2342354;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
		int diskNumber = 21321;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 87696796;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		builder.setComment(null);
		assertNull(builder.getComment());
		assertNull(builder.getCommentBytes());
		String comment = "fewjpfjewp";
		byte[] commentBytes = comment.getBytes();
		builder.setComment(comment);
		assertEquals(comment, builder.getComment());
		byte[] extensibleData = "ewewfewfewfewfwef".getBytes();
		builder.setExtensibleData(extensibleData);
		assertEquals(extensibleData, builder.getExtensibleData());
		builder.setCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());
		int numberDisks = 5254;
		builder.setNumberDisks(numberDisks);
		assertEquals(numberDisks, builder.getNumberDisks());

		ZipCentralDirectoryEndInfo endInfo = builder.build();
		assertEquals(zip64, endInfo.isNeedsZip64());
		assertEquals(versionMade, endInfo.getVersionMade());
		assertEquals(versionNeeded, endInfo.getVersionNeeded());
		assertEquals(diskNumber, endInfo.getDiskNumber());
		assertEquals(diskNumberStart, endInfo.getDiskNumberStart());
		assertEquals(comment, endInfo.getComment());
		assertEquals(extensibleData, endInfo.getExtensibleData());
		assertEquals(commentBytes, endInfo.getCommentBytes());
		assertEquals(numberDisks, endInfo.getNumberDisks());
	}

	@Test
	public void testFromEnd() {
		Zip64CentralDirectoryEnd.Builder endBuilder = Zip64CentralDirectoryEnd.builder();

		int versionMade = 2354;
		endBuilder.setVersionMade(versionMade);
		int versionNeeded = 22354;
		endBuilder.setVersionNeeded(versionNeeded);
		int diskNumber = 131221321;
		endBuilder.setDiskNumber(diskNumber);
		int diskNumberStart = 525131212;
		endBuilder.setDiskNumberStart(diskNumberStart);
		byte[] extensibleData = "32131231231".getBytes();
		endBuilder.setExtensibleData(extensibleData);

		ZipCentralDirectoryEndInfo.Builder builder =
				ZipCentralDirectoryEndInfo.Builder.fromCentralDirectoryEnd(endBuilder.build());
		ZipCentralDirectoryEndInfo endInfo = builder.build();
		assertTrue(endInfo.isNeedsZip64());
		assertEquals(versionMade, endInfo.getVersionMade());
		assertEquals(versionNeeded, endInfo.getVersionNeeded());
		assertEquals(diskNumber, endInfo.getDiskNumber());
		assertEquals(diskNumberStart, endInfo.getDiskNumberStart());
		assertEquals(extensibleData, endInfo.getExtensibleData());
	}

	@Test
	public void testBuildWith() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();

		boolean zip64 = true;
		builder.withZip64(zip64);
		int versionMade = 2354;
		builder.withVersionMade(versionMade);
		int versionNeeded = 2342354;
		builder.withVersionNeeded(versionNeeded);
		int diskNumber = 21321;
		builder.withDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 87696796;
		builder.withDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		builder.withComment(null);
		assertNull(builder.getComment());
		assertNull(builder.getCommentBytes());
		String comment = "fewjpfjewp";
		byte[] commentBytes = comment.getBytes();
		builder.withComment(comment);
		assertEquals(comment, builder.getComment());
		byte[] extensibleData = "ewewfewfewfewfwef".getBytes();
		builder.withExtensibleData(extensibleData);
		builder.withCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());
		int numberDisks = 5254;
		builder.withNumberDisks(numberDisks);

		ZipCentralDirectoryEndInfo endInfo = builder.build();
		assertEquals(zip64, endInfo.isNeedsZip64());
		assertEquals(versionMade, endInfo.getVersionMade());
		assertEquals(versionNeeded, endInfo.getVersionNeeded());
		assertEquals(diskNumber, endInfo.getDiskNumber());
		assertEquals(diskNumberStart, endInfo.getDiskNumberStart());
		assertEquals(comment, endInfo.getComment());
		assertEquals(commentBytes, endInfo.getCommentBytes());
		assertEquals(extensibleData, endInfo.getExtensibleData());
		assertEquals(numberDisks, endInfo.getNumberDisks());
	}

	@Test
	public void testNoComment() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();
		assertNull(builder.getComment());
		ZipCentralDirectoryEndInfo fileInfo = builder.build();
		assertNull(fileInfo.getComment());
		assertNull(fileInfo.getCommentBytes());
	}

	@Test
	public void testFromEndInfo() {
		ZipCentralDirectoryEnd.Builder endBuilder = ZipCentralDirectoryEnd.builder();
		int diskNumber = 21321;
		endBuilder.setDiskNumber(diskNumber);
		int diskNumberStart = 8796;
		endBuilder.setDiskNumberStart(diskNumberStart);
		String comment = "fewjpfjewp";
		byte[] commentBytes = comment.getBytes();
		endBuilder.setCommentBytes(commentBytes);

		ZipCentralDirectoryEndInfo.Builder endInfoBuilder =
				ZipCentralDirectoryEndInfo.Builder.fromCentralDirectoryEnd(endBuilder.build());
		assertEquals(diskNumber, endInfoBuilder.getDiskNumber());
		assertEquals(diskNumberStart, endInfoBuilder.getDiskNumberStart());
		assertEquals(comment, endInfoBuilder.getComment());
		assertEquals(commentBytes, endInfoBuilder.getCommentBytes());
	}

	@Test
	public void testNeedsZip64() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();
		assertFalse(builder.hasZip64Values());
		assertFalse(builder.build().isNeedsZip64());

		builder = ZipCentralDirectoryEndInfo.builder();
		builder.setVersionMade(1);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder = ZipCentralDirectoryEndInfo.builder();
		builder.setVersionNeeded(1);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder = ZipCentralDirectoryEndInfo.builder();
		builder.setDiskNumber(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder = ZipCentralDirectoryEndInfo.builder();
		builder.setDiskNumberStart(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder = ZipCentralDirectoryEndInfo.builder();
		builder.setNumberDisks(1);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());
	}
}
