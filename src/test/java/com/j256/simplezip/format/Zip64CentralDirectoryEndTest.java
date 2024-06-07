package com.j256.simplezip.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.Zip64CentralDirectoryEnd.Builder;

public class Zip64CentralDirectoryEndTest {

	@Test
	public void testCoverageZip64() throws IOException {
		Builder builder = Zip64CentralDirectoryEnd.builder();

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

		Zip64CentralDirectoryEnd dirEnd = builder.build();
		assertEquals(versionMade, dirEnd.getVersionMade());
		assertEquals(versionNeeded, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertEquals(extensibleData, dirEnd.getExtensibleData());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dirEnd.write(baos);

		dirEnd = Zip64CentralDirectoryEnd
				.read(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 10240));
		assertEquals(versionMade, dirEnd.getVersionMade());
		assertEquals(versionNeeded, dirEnd.getVersionNeeded());
		assertEquals(diskNumber, dirEnd.getDiskNumber());
		assertEquals(diskNumberStart, dirEnd.getDiskNumberStart());
		assertEquals(numRecordsOnDisk, dirEnd.getNumRecordsOnDisk());
		assertEquals(numRecordsTotal, dirEnd.getNumRecordsTotal());
		assertEquals(directorySize, dirEnd.getDirectorySize());
		assertEquals(directoryOffset, dirEnd.getDirectoryOffset());
		assertArrayEquals(extensibleData, dirEnd.getExtensibleData());
	}

	@Test
	public void testReadWrong() throws IOException {
		assertNull(Zip64CentralDirectoryEnd
				.read(new RewindableInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }), 1024)));
	}
}
