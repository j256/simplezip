package com.j256.simplezip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.j256.simplezip.format.ZipCentralDirectoryEnd;

public class DiskFileTest {

	@Test
	public void testEmptyFile() throws IOException {
		InputStream stream = getClass().getResourceAsStream("/empty.zip");
		assertNotNull(stream);
		ZipFileInput input = new ZipFileInput(stream);
		assertNull(input.readFileHeader());
		assertNull(input.readDirectoryFileEntry());
		ZipCentralDirectoryEnd end = input.readDirectoryEnd();
		assertNotNull(end);
		assertEquals("hello", end.getComment());
		input.close();
	}
}
