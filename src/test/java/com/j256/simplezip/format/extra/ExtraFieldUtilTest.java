package com.j256.simplezip.format.extra;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.IoUtils;

public class ExtraFieldUtilTest {

	@Test
	public void testStuff() throws IOException {
		assertNull(ExtraFieldUtil.readExtraField(new ByteArrayInputStream(new byte[] { 1 }), true));
		assertNull(
				ExtraFieldUtil.readExtraField(new ByteArrayInputStream(idToBytes(Zip64ExtraField.EXPECTED_ID)), true));
		assertTrue(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idAndSizeToBytes(Zip64ExtraField.EXPECTED_ID, 0)),
				true) instanceof UnknownExtraField);
		assertNull(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idToBytes(ExtendedTimestampCentralExtraField.EXPECTED_ID)), true));
		assertTrue(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idAndSizeToBytes(ExtendedTimestampCentralExtraField.EXPECTED_ID, 0)),
				true) instanceof UnknownExtraField);
		assertNull(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idToBytes(ExtendedTimestampCentralExtraField.EXPECTED_ID)), false));
		assertTrue(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idAndSizeToBytes(ExtendedTimestampCentralExtraField.EXPECTED_ID, 0)),
				false) instanceof UnknownExtraField);
		assertNull(
				ExtraFieldUtil.readExtraField(new ByteArrayInputStream(idToBytes(Unix1ExtraField.EXPECTED_ID)), true));
		assertTrue(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idAndSizeToBytes(Unix1ExtraField.EXPECTED_ID, 0)),
				true) instanceof UnknownExtraField);
		assertNull(
				ExtraFieldUtil.readExtraField(new ByteArrayInputStream(idToBytes(Unix2ExtraField.EXPECTED_ID)), true));
		assertTrue(ExtraFieldUtil.readExtraField(
				new ByteArrayInputStream(idAndSizeToBytes(Unix2ExtraField.EXPECTED_ID, 0)),
				true) instanceof UnknownExtraField);
	}

	private byte[] idToBytes(int id) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] tmpBytes = new byte[8];
		IoUtils.writeShort(baos, tmpBytes, id);
		return baos.toByteArray();
	}

	private byte[] idAndSizeToBytes(int id, int size) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] tmpBytes = new byte[8];
		IoUtils.writeShort(baos, tmpBytes, id);
		IoUtils.writeShort(baos, tmpBytes, size);
		return baos.toByteArray();
	}
}
