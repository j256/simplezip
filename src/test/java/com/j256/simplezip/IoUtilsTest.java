package com.j256.simplezip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.junit.Test;

public class IoUtilsTest {

	@Test
	public void testReadWriteByte() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b = 121;
		IoUtils.writeByte(baos, b);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		assertEquals(b, IoUtils.readByte(bais, "stuff"));
	}

	@Test
	public void testReadWriteShort() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int s = 14321;
		byte[] tmpBytes = new byte[8];
		IoUtils.writeShort(baos, tmpBytes, s);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		assertEquals(s, IoUtils.readShort(bais, tmpBytes, "stuff"));
	}

	@Test
	public void testReadWriteInt() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = 1434522321;
		byte[] tmpBytes = new byte[8]; 
		IoUtils.writeInt(baos, tmpBytes, i);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		assertEquals(i, IoUtils.readInt(bais, tmpBytes, "stuff"));
	}

	@Test
	public void testReadWriteLong() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long l = 14342342345534321L;
		byte[] tmpBytes = new byte[8]; 
		IoUtils.writeLong(baos, tmpBytes, l);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		assertEquals(l, IoUtils.readLong(bais, tmpBytes, "stuff"));
	}

	@Test
	public void testReadWriteBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[] { 4, 5, 6, 4, 31, 3, 44, (byte) 140, 76, 68, 9, 97, 87, (byte) 255 };
		IoUtils.writeBytes(baos, bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		assertArrayEquals(bytes, IoUtils.readBytes(bais, bytes.length, "stuff"));
	}

	@Test(expected = EOFException.class)
	public void testEof() throws EOFException, IOException {
		IoUtils.readByte(new ByteArrayInputStream(new byte[0]), "stuff");
	}

	@Test
	public void testReadZeroBytes() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		assertArrayEquals(new byte[0], IoUtils.readBytes(bais, 0, "stuff"));
	}

	@Test(expected = EOFException.class)
	public void testReadBytesEof() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		assertArrayEquals(new byte[0], IoUtils.readBytes(bais, 10, "stuff"));
	}
}
