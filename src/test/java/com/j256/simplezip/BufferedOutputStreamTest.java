package com.j256.simplezip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.format.ZipFileHeader;

public class BufferedOutputStreamTest {

	@Test
	public void testCoverage() throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new ByteArrayOutputStream());
		bos.enableBuffer(Long.MAX_VALUE, 0);
		bos.setFileHeader(ZipFileHeader.builder().build());
		assertNull(bos.getTmpFile());
		bos.write(1);
		File file = bos.getTmpFile();
		assertNotNull(file);
		assertEquals(1, file.length());
		bos.close();
		assertFalse(file.exists());
	}
}
