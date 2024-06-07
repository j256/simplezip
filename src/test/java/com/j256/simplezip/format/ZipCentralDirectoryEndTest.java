package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.ZipCentralDirectoryEnd.Builder;

public class ZipCentralDirectoryEndTest {

	@Test
	public void testCoverage() {
		Builder builder = ZipCentralDirectoryEnd.builder();

		int diskNumber = 1312;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 5251;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int numRecordsOnDisk = 567;
		builder.setNumRecordsOnDisk(numRecordsOnDisk);
		assertEquals(numRecordsOnDisk, builder.getNumRecordsOnDisk());
		int numRecordsTotal = 624;
		builder.setNumRecordsTotal(numRecordsTotal);
		assertEquals(numRecordsTotal, builder.getNumRecordsTotal());
		int directorySize = 322342434;
		builder.setDirectorySize(directorySize);
		assertEquals(directorySize, builder.getDirectorySize());
		int directoryOffset = 8567056;
		builder.setDirectoryOffset(directoryOffset);
		assertEquals(directoryOffset, builder.getDirectoryOffset());
		String comment = "comment 234231";
		byte[] commentBytes = comment.getBytes();
		builder.setCommentBytes(commentBytes);
		assertEquals(commentBytes, builder.getCommentBytes());

		ZipCentralDirectoryEnd dirEnd = builder.build();
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertArrayEquals(commentBytes, dirEnd.getCommentBytes());
		assertEquals(comment, dirEnd.getComment());
	}

	@Test
	public void testNoCommernt() {
		Builder builder = ZipCentralDirectoryEnd.builder();
		assertNull(builder.getCommentBytes());

		ZipCentralDirectoryEnd dirEnd = builder.build();
		assertNull(dirEnd.getCommentBytes());
		assertNull(dirEnd.getComment());
	}

	@Test
	public void testReadWrong() throws IOException {
		assertNull(ZipCentralDirectoryEnd
				.read(new RewindableInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }), 1024)));
	}

	@Test
	public void testNeedsZip64() {
		Builder builder = ZipCentralDirectoryEnd.builder();
		assertFalse(builder.hasZip64Values());
		assertFalse(builder.build().isNeedsZip64());

		builder.setDiskNumber(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder.setDiskNumberStart(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder.setNumRecordsOnDisk(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder.setNumRecordsTotal(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder.setDirectorySize(IoUtils.MAX_UNSIGNED_INT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());

		builder.setDirectoryOffset(IoUtils.MAX_UNSIGNED_INT_VALUE);
		assertTrue(builder.hasZip64Values());
		assertTrue(builder.build().isNeedsZip64());
	}
}
