package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.ExtendedTimestampCentralExtraField.Builder;

public class ExtendedTimestampCentralExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = ExtendedTimestampCentralExtraField.builder();

		ExtendedTimestampCentralExtraField field = builder.build();
		assertFalse(field.isTimeModified());
		assertFalse(field.isTimeAccessed());
		assertFalse(field.isTimeCreated());

		int flags = 0;
		boolean timeModified = false;
		builder.setTimeModified(timeModified);
		assertEquals(timeModified, builder.isTimeModified());
		timeModified = true;
		builder.setTimeModified(timeModified);
		assertEquals(timeModified, builder.isTimeModified());
		boolean timeAccessed = false;
		builder.setTimeAccessed(timeAccessed);
		assertEquals(timeAccessed, builder.isTimeAccessed());
		timeAccessed = true;
		builder.setTimeAccessed(timeAccessed);
		assertEquals(timeAccessed, builder.isTimeAccessed());
		boolean timeCreated = false;
		builder.setTimeCreated(timeCreated);
		assertEquals(timeCreated, builder.isTimeCreated());
		timeCreated = true;
		builder.setTimeCreated(timeCreated);
		assertEquals(timeCreated, builder.isTimeCreated());
		flags |= ExtendedTimestampCentralExtraField.TIME_MODIFIED_FLAG;
		flags |= ExtendedTimestampCentralExtraField.TIME_ACCESSED_FLAG;
		flags |= ExtendedTimestampCentralExtraField.TIME_CREATED_FLAG;
		assertEquals(flags, builder.getFlags());
		long time = 5251312;
		builder.setTime(time);
		assertEquals(time, (long) builder.getTime());

		field = builder.build();
		assertEquals(flags, field.getFlags());
		assertEquals(time, (long) field.getTime());
		assertEquals(timeModified, field.isTimeModified());
		assertEquals(timeAccessed, field.isTimeAccessed());
		assertEquals(timeCreated, field.isTimeCreated());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = ExtendedTimestampCentralExtraField.builder();

		int flags = 112;
		builder.setFlags(flags);
		long time = 5251312;
		builder.setTime(time);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), false);
		assertTrue(field instanceof ExtendedTimestampCentralExtraField);
		ExtendedTimestampCentralExtraField castField = (ExtendedTimestampCentralExtraField) field;

		assertEquals(flags, castField.getFlags());
		assertEquals(time, (long) castField.getTime());
	}

	@Test
	public void testReadWriteNoTime() throws IOException {
		Builder builder = ExtendedTimestampCentralExtraField.builder();

		int flags = 112;
		builder.setFlags(flags);
		Long time = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), false);
		assertTrue(field instanceof ExtendedTimestampCentralExtraField);
		ExtendedTimestampCentralExtraField castField = (ExtendedTimestampCentralExtraField) field;

		assertEquals(flags, castField.getFlags());
		assertEquals(time, castField.getTime());
	}
}
