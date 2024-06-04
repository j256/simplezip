package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.ZipStatus;
import com.j256.simplezip.format.extra.UnknownExtraField.Builder;

public class UnknownExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = UnknownExtraField.builder();

		assertNull(builder.build().getBytes());

		byte[] bytes = new byte[] { 1, 54, 32, 51 };
		builder.setBytes(bytes);
		assertArrayEquals(bytes, builder.getBytes());
		bytes = new byte[] { 1, 54, 32, 5, 72, 51 };
		builder.withBytes(bytes);
		assertArrayEquals(bytes, builder.getBytes());

		int id = 13123;
		builder.setId(id);
		assertEquals(id, builder.getId());
		id = 12313124;
		builder.withId(id);
		assertEquals(id, builder.getId());

		UnknownExtraField field = builder.build();
		assertArrayEquals(bytes, field.getBytes());
		assertEquals(id, field.getId());
		assertEquals(ZipStatus.OK, field.validate());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = UnknownExtraField.builder();

		byte[] bytes = new byte[] { 1, 54, 32, 51 };
		builder.setBytes(bytes);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), true);
		assertTrue(field instanceof UnknownExtraField);
		UnknownExtraField castField = (UnknownExtraField) field;

		assertArrayEquals(bytes, castField.getBytes());
	}
}
