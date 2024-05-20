package com.j256.simplezip.format.extra;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.UnknownExtraField.Builder;

public class UnknownExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = UnknownExtraField.builder();

		byte[] bytes = new byte[] { 1, 54, 32, 51 };
		builder.setBytes(bytes);
		assertArrayEquals(bytes, builder.getBytes());

		UnknownExtraField field = builder.build();
		assertArrayEquals(bytes, field.getBytes());
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
