package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.simplezip.format.ZipCentralDirectoryEnd.Builder;

public class CentralDirectoryEndTest {

	@Test
	public void testCoverage() {
		Builder builder = ZipCentralDirectoryEnd.builder();

		int diskNumber = 1312;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 5251312;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int numRecordsOnDisk = 565479567;
		builder.setNumRecordsOnDisk(numRecordsOnDisk);
		assertEquals(numRecordsOnDisk, builder.getNumRecordsOnDisk());
		int numRecordsTotal = 6334324;
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
}
