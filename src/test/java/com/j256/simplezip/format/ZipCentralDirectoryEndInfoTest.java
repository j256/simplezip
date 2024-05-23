package com.j256.simplezip.format;

import static org.junit.Assert.*;

import org.junit.Test;

public class ZipCentralDirectoryEndInfoTest {

	@Test
	public void testCoverage() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();

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
		builder.setCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());

		ZipCentralDirectoryEndInfo endInfo = builder.build();
		assertEquals(diskNumber, endInfo.getDiskNumber());
		assertEquals(diskNumberStart, endInfo.getDiskNumberStart());
		assertEquals(comment, endInfo.getComment());
		assertEquals(commentBytes, endInfo.getCommentBytes());
	}

	@Test
	public void testBuildWith() {
		ZipCentralDirectoryEndInfo.Builder builder = ZipCentralDirectoryEndInfo.builder();

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
		builder.withCommentBytes(commentBytes);
		assertArrayEquals(commentBytes, builder.getCommentBytes());

		ZipCentralDirectoryEndInfo endInfo = builder.build();
		assertEquals(diskNumber, endInfo.getDiskNumber());
		assertEquals(diskNumberStart, endInfo.getDiskNumberStart());
		assertEquals(comment, endInfo.getComment());
		assertEquals(commentBytes, endInfo.getCommentBytes());
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
	public void testFromEnd() {
		ZipCentralDirectoryEnd.Builder endBuilder = ZipCentralDirectoryEnd.builder();
		int diskNumber = 21321;
		endBuilder.setDiskNumber(diskNumber);
		int diskNumberStart = 87696796;
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
}
