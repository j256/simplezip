package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class RewindableInputStreamTest {

	@Test
	public void testStuff() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		RewindableInputStream ris = new RewindableInputStream(bais, 10);
		assertEquals(bytes[0], ris.read());

		byte[] readBytes = new byte[2];
		assertEquals(readBytes.length, ris.read(readBytes));
		assertEquals(bytes[1], readBytes[0]);
		assertEquals(bytes[2], readBytes[1]);

		assertEquals(1, ris.read(readBytes, 1, 1));
		assertEquals(bytes[1], readBytes[0]);
		assertEquals(bytes[3], readBytes[1]);

		readBytes = new byte[10];
		assertEquals(4, ris.read(readBytes));
		assertEquals(bytes[4], readBytes[0]);
		assertEquals(bytes[5], readBytes[1]);
		assertEquals(bytes[6], readBytes[2]);
		assertEquals(bytes[7], readBytes[3]);

		ris.rewind(4);
		assertEquals(2, ris.read(readBytes, 0, 2));
		assertEquals(bytes[4], readBytes[0]);
		assertEquals(bytes[5], readBytes[1]);
		assertEquals(2, ris.read(readBytes, 0, readBytes.length));
		assertEquals(bytes[6], readBytes[0]);
		assertEquals(bytes[7], readBytes[1]);

		assertEquals(-1, ris.read());
		assertEquals(-1, ris.read(readBytes));
		ris.close();
	}

	@Test(expected = IOException.class)
	public void testRewindTooMuch() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		RewindableInputStream ris = new RewindableInputStream(bais, 10);

		byte[] readBytes = new byte[2];
		assertEquals(readBytes.length, ris.read(readBytes));
		assertEquals(bytes[0], readBytes[0]);
		assertEquals(bytes[1], readBytes[1]);

		ris.rewind(readBytes.length + 1);
		ris.close();
	}

	@Test
	public void testEnsureExpand() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		RewindableInputStream ris = new RewindableInputStream(bais, 10);

		byte[] readBytes = new byte[1000];
		int numRead = ris.read(readBytes);
		assertEquals(bytes.length, numRead);
		assertArrayEquals(bytes, Arrays.copyOf(readBytes, numRead));

		ris.close();
	}

	@Test
	public void testRewindOneByte() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		RewindableInputStream ris = new RewindableInputStream(bais, 10);
		assertEquals(bytes[0], ris.read());

		byte[] readBytes = new byte[10];
		assertEquals(bytes.length - 1, ris.read(readBytes));

		ris.rewind(2);

		assertEquals(bytes[6], ris.read());
		assertEquals(bytes[7], ris.read());
		assertEquals(-1, ris.read());

		ris.close();
	}

	@Test
	public void testRewindThenExtendBuffer() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		RewindableInputStream ris = new RewindableInputStream(bais, 10);

		byte[] readBytes = new byte[10];
		assertEquals(bytes.length, ris.read(readBytes));

		ris.rewind(4);

		assertEquals(bytes[4], ris.read());
		assertEquals(bytes[5], ris.read());

		readBytes = new byte[100];
		assertEquals(2, ris.read(readBytes));
		assertEquals(bytes[6], readBytes[0]);
		assertEquals(bytes[7], readBytes[1]);
		assertEquals(-1, ris.read());

		ris.close();
	}

	@Test
	public void testCoverage() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		RewindableInputStream ris = new RewindableInputStream(bais, 10);

		byte[] readBytes = new byte[2];
		assertEquals(0, ris.read(readBytes, 0, 0));

		ris.close();
	}
}
