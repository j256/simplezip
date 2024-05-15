package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.Unix1ExtraField.Builder;

public class Unix1ExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = Unix1ExtraField.builder();

		long timeLastModified = 3458995048034583904L;
		builder.setTimeLastModified(timeLastModified);
		assertEquals(timeLastModified, builder.getTimeLastModified());
		long timeLastAccessed = 995534534454534355L;
		builder.setTimeLastAccessed(timeLastAccessed);
		assertEquals(timeLastAccessed, builder.getTimeLastAccessed());
		int userId = 4423;
		builder.setUserId(userId);
		assertEquals(userId, (int) builder.getUserId());
		int groupId = 213;
		builder.setGroupId(groupId);
		assertEquals(groupId, (int) builder.getGroupId());

		Unix1ExtraField field = builder.build();
		assertEquals(timeLastModified, field.getTimeLastModified());
		assertEquals(timeLastAccessed, field.getTimeLastAccessed());
		assertEquals(userId, (int) field.getUserId());
		assertEquals(groupId, (int) field.getGroupId());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = Unix1ExtraField.builder();

		long timeLastModified = 3458995048034583904L;
		builder.setTimeLastModified(timeLastModified);
		long timeLastAccessed = 995534534454534355L;
		builder.setTimeLastAccessed(timeLastAccessed);
		int userId = 4423;
		builder.setUserId(userId);
		int groupId = 213;
		builder.setGroupId(groupId);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), true);
		assertTrue(field instanceof Unix1ExtraField);
		Unix1ExtraField castField = (Unix1ExtraField) field;

		assertEquals(timeLastModified, castField.getTimeLastModified());
		assertEquals(timeLastAccessed, castField.getTimeLastAccessed());
		assertEquals(userId, (int) castField.getUserId());
		assertEquals(groupId, (int) castField.getGroupId());
	}
}
