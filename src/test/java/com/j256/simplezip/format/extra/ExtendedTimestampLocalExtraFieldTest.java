package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.ExtendedTimestampLocalExtraField.Builder;

public class ExtendedTimestampLocalExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = ExtendedTimestampLocalExtraField.builder();

		int flags = 232434230;
		builder.setFlags(flags);
		assertEquals(flags, builder.getFlags());
		long timeLastModified = 3458995048034583904L;
		builder.setTimeLastModified(timeLastModified);
		assertEquals(timeLastModified, builder.getTimeLastModified());
		long timeLastAccessed = 995534534454534355L;
		builder.setTimeLastAccessed(timeLastAccessed);
		assertEquals(timeLastAccessed, builder.getTimeLastAccessed());
		long timeCreated = 3243424353443223434L;
		builder.setTimeCreation(timeCreated);
		assertEquals(timeCreated, builder.getTimeCreation());

		ExtendedTimestampLocalExtraField field = builder.build();
		assertEquals(flags, field.getFlags());
		assertEquals(timeLastModified, field.getTimeLastModified());
		assertEquals(timeLastAccessed, field.getTimeLastAccessed());
		assertEquals(timeCreated, field.getTimeCreation());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = ExtendedTimestampLocalExtraField.builder();

		int flags = 233;
		builder.setFlags(flags);
		long timeLastModified = 3458995048034583904L;
		builder.setTimeLastModified(timeLastModified);
		long timeLastAccessed = 995534534454534355L;
		builder.setTimeLastAccessed(timeLastAccessed);
		long timeCreated = 3243424353443223434L;
		builder.setTimeCreation(timeCreated);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), true);
		assertTrue(field instanceof ExtendedTimestampLocalExtraField);
		ExtendedTimestampLocalExtraField castField = (ExtendedTimestampLocalExtraField) field;

		assertEquals(flags, castField.getFlags());
		assertEquals(timeLastModified, castField.getTimeLastModified());
		assertEquals(timeLastAccessed, castField.getTimeLastAccessed());
		assertEquals(timeCreated, castField.getTimeCreation());
	}
}
