package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator.Builder;

public class Zip64CentralDirectoryEndLocatorTest {

	@Test
	public void testCoverage() throws IOException {
		Builder builder = Zip64CentralDirectoryEndLocator.builder();
		int diskNumber = 123;
		builder.setDiskNumber(diskNumber);
		assertEquals(diskNumber, builder.getDiskNumber());
		int diskNumberStart = 123113;
		builder.setDiskNumberStart(diskNumberStart);
		assertEquals(diskNumberStart, builder.getDiskNumberStart());
		long endOffset = 1231131313L;
		builder.setEndOffset(endOffset);
		assertEquals(endOffset, builder.getEndOffset());
		int numberDisks = 13214;
		builder.setNumberDisks(numberDisks);
		assertEquals(numberDisks, builder.getNumberDisks());

		Zip64CentralDirectoryEndLocator locator = builder.build();
		assertEquals(diskNumber, locator.getDiskNumber());
		assertEquals(diskNumberStart, locator.getDiskNumberStart());
		assertEquals(endOffset, locator.getEndOffset());
		assertEquals(numberDisks, locator.getNumberDisks());

		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		locator.write(boas);

		locator = Zip64CentralDirectoryEndLocator
				.read(new RewindableInputStream(new ByteArrayInputStream(boas.toByteArray()), 10240));
		assertEquals(diskNumber, locator.getDiskNumber());
		assertEquals(diskNumberStart, locator.getDiskNumberStart());
		assertEquals(endOffset, locator.getEndOffset());
		assertEquals(numberDisks, locator.getNumberDisks());
	}

	@Test
	public void testReadNotUs() throws IOException {
		assertNull(Zip64CentralDirectoryEndLocator
				.read(new RewindableInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }), 10240)));
	}

	@Test
	public void testFromEndInfo() {
		ZipCentralDirectoryEndInfo.Builder endBuilder = ZipCentralDirectoryEndInfo.builder();
		int diskNumber = 21321;
		endBuilder.setDiskNumber(diskNumber);
		int diskNumberStart = 87696796;
		endBuilder.setDiskNumberStart(diskNumberStart);
		int numberDisks = 131251541;
		endBuilder.setNumberDisks(numberDisks);

		Zip64CentralDirectoryEndLocator.Builder locator =
				Zip64CentralDirectoryEndLocator.Builder.fromEndInfo(endBuilder.build());
		assertEquals(diskNumber, locator.getDiskNumber());
		assertEquals(diskNumberStart, locator.getDiskNumberStart());
		assertEquals(numberDisks, locator.getNumberDisks());
	}
}
