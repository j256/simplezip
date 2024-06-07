package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.ZipCentralDirectoryEnd.Builder;

public class ZipCentralDirectoryEndTest {

	@Test
	public void testCoverageZip32() throws IOException {
		Builder builder = ZipCentralDirectoryEnd.builder();

		int diskNumber = 1312;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 2512;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		int numRecordsOnDisk = 5657;
		builder.setNumRecordsOnDisk(numRecordsOnDisk);
		assertEquals(numRecordsOnDisk, builder.getNumRecordsOnDisk());
		int numRecordsTotal = 6344;
		builder.setNumRecordsTotal(numRecordsTotal);
		assertEquals(numRecordsTotal, builder.getNumRecordsTotal());
		int directorySize = 323434;
		builder.setDirectorySize(directorySize);
		assertEquals(directorySize, builder.getDirectorySize());
		int directoryOffset = 87056;
		builder.setDirectoryOffset(directoryOffset);
		assertEquals(directoryOffset, builder.getDirectoryOffset());
		String comment = "comment 234231";
		byte[] commentBytes = comment.getBytes();
		builder.setCommentBytes(commentBytes);
		assertEquals(commentBytes, builder.getCommentBytes());

		ZipCentralDirectoryEnd dirEnd = builder.build();
		assertFalse(dirEnd.isZip64());
		assertEquals(0, dirEnd.getVersionMade());
		assertEquals(0, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertArrayEquals(commentBytes, dirEnd.getCommentBytes());
		assertEquals(comment, dirEnd.getComment());
		assertNull(dirEnd.getExtensibleData());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dirEnd.write(baos);

		dirEnd = ZipCentralDirectoryEnd
				.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240));
		assertFalse(dirEnd.isZip64());
		assertEquals(0, dirEnd.getVersionMade());
		assertEquals(0, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertArrayEquals(commentBytes, dirEnd.getCommentBytes());
		assertEquals(comment, dirEnd.getComment());
		assertNull(dirEnd.getExtensibleData());
	}

	@Test
	public void testCoverageZip64() throws IOException {
		Builder builder = ZipCentralDirectoryEnd.builder();

		int versionMade = 2354;
		builder.setVersionMade(versionMade);
		assertEquals(versionMade, builder.getVersionMade());
		int versionNeeded = 22354;
		builder.setVersionNeeded(versionNeeded);
		assertEquals(versionNeeded, builder.getVersionNeeded());
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
		byte[] extensibleData = "32131231231".getBytes();
		builder.setExtensibleData(extensibleData);
		assertEquals(extensibleData, builder.getExtensibleData());

		ZipCentralDirectoryEnd dirEnd = builder.build();
		assertEquals(versionMade, dirEnd.getVersionMade());
		assertEquals(versionNeeded, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertNull(dirEnd.getComment());
		assertEquals(extensibleData, dirEnd.getExtensibleData());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dirEnd.write(baos);

		dirEnd = ZipCentralDirectoryEnd
				.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240));
		assertEquals(versionMade, dirEnd.getVersionMade());
		assertEquals(versionNeeded, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertNull(dirEnd.getCommentBytes());
		assertArrayEquals(extensibleData, dirEnd.getExtensibleData());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBothCommentExtesibleData() {
		Builder builder = ZipCentralDirectoryEnd.builder();

		byte[] commentBytes = "comment 234231".getBytes();
		builder.setCommentBytes(commentBytes);
		byte[] extensibleData = "32131231231".getBytes();
		builder.setExtensibleData(extensibleData);

		builder.build();
	}

	@Test
	public void testZip64Triggers() {
		Builder builder = ZipCentralDirectoryEnd.builder();
		assertFalse(builder.build().isZip64());

		builder.setVersionMade(1);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setVersionNeeded(1);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setDiskNumber(100000);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setDiskNumberStart(100000);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setNumRecordsOnDisk(100000);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setNumRecordsTotal(100000);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setDirectorySize(10000000000L);
		assertTrue(builder.build().isZip64());

		builder = ZipCentralDirectoryEnd.builder();
		builder.setDirectoryOffset(10000000000L);
		assertTrue(builder.build().isZip64());
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
}
