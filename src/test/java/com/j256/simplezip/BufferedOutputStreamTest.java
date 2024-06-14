package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;

public class BufferedOutputStreamTest {

	@Test
	public void testCoverage() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		bos.enableBuffer(Long.MAX_VALUE, 0);
		Builder headerBuilder = ZipFileHeader.builder();
		bos.setFileHeader(headerBuilder.build());
		assertNull(bos.getTmpFile());
		bos.write(1);
		File file = bos.getTmpFile();
		assertNotNull(file);
		assertEquals(1, file.length());
		bos.finishFileData(0, 0);
		headerBuilder.setCompressedSize(1);
		headerBuilder.build().write(expected);
		expected.write(1);
		assertArrayEquals(expected.toByteArray(), baos.toByteArray());
		bos.close();
		assertFalse(file.exists());
	}

	@Test
	public void testIncreasingBufferSize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		bos.enableBuffer(Long.MAX_VALUE, 0);
		Builder headerBuilder = ZipFileHeader.builder();
		bos.setFileHeader(headerBuilder.build());
		assertNull(bos.getTmpFile());
		byte[] buf1 = new byte[] { 1, 2, 3 };
		bos.write(buf1, 0, buf1.length);
		File file = bos.getTmpFile();
		assertNotNull(file);
		byte[] buf2 = new byte[IoUtils.STANDARD_BUFFER_SIZE * 10];
		new Random().nextBytes(buf2);
		bos.write(buf2, 0, buf2.length);
		bos.flush();
		assertEquals(buf1.length + buf2.length, file.length());
		bos.finishFileData(0, 0);
		assertFalse(file.exists());
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		headerBuilder.setCompressedSize(buf1.length + buf2.length);
		headerBuilder.build().write(expected);
		expected.write(buf1);
		expected.write(buf2);
		assertArrayEquals(expected.toByteArray(), baos.toByteArray());
		bos.close();
	}
}
