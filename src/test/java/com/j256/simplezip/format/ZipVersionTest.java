package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ZipVersionTest {

	@Test
	public void testCoverage() {
		assertEquals(ZipVersion.UNKNOWN, ZipVersion.fromValue(1231232131));
		ZipVersion version = ZipVersion.V2_5;
		assertEquals(2, version.getMajor());
		assertEquals(5, version.getMinor());
		assertEquals(version.getMajor() + "." + version.getMinor(), version.getVersionString());
	}
}
