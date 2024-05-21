package com.j256.simplezip;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.simplezip.ZipStatus.ZipStatusId;

public class ZipStatusTest {

	@Test
	public void testCoverage() {
		ZipStatusId id = ZipStatusId.DESCRIPTOR_CRC_NO_MATCH;
		String msg = "oh no!";
		ZipStatus status = new ZipStatus(id, msg);
		assertEquals(id, status.getId());
		assertEquals(msg, status.getMessage());
	}
}
