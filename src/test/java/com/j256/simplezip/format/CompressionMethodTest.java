package com.j256.simplezip.format;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompressionMethodTest {

	@Test
	public void testCoverage() {
		assertEquals(CompressionMethod.UNKNOWN, CompressionMethod.fromValue(100000));
	}
}
