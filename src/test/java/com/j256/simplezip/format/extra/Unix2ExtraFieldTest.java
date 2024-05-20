package com.j256.simplezip.format.extra;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.format.extra.Unix2ExtraField.Builder;

public class Unix2ExtraFieldTest {

	@Test
	public void testCoverage() {
		Builder builder = Unix2ExtraField.builder();

		int userId = 342;
		builder.setUserId(userId);
		assertEquals(userId, builder.getUserId());
		int groupId = 993;
		builder.setGroupId(groupId);
		assertEquals(groupId, builder.getGroupId());

		Unix2ExtraField field = builder.build();
		assertEquals(userId, field.getUserId());
		assertEquals(groupId, field.getGroupId());
	}

	@Test
	public void testReadWrite() throws IOException {
		Builder builder = Unix2ExtraField.builder();

		int userId = 342;
		builder.setUserId(userId);
		int groupId = 993;
		builder.setGroupId(groupId);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		builder.build().write(baos);

		BaseExtraField field = ExtraFieldUtil
				.readExtraField(new RewindableInputStream(new ByteArrayInputStream(baos.toByteArray()), 1024), true);
		assertTrue(field instanceof Unix2ExtraField);
		Unix2ExtraField castField = (Unix2ExtraField) field;

		assertEquals(userId, castField.getUserId());
		assertEquals(groupId, castField.getGroupId());
	}
}
