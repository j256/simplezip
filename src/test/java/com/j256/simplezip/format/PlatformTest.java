package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlatformTest {

	@Test
	public void testStuff() {
		assertEquals(Platform.OTHER, Platform.detectPlatform(null));
		assertEquals(Platform.UNIX, Platform.detectPlatform("OS X 10.5.4"));
		assertEquals(Platform.WINDOWS, Platform.detectPlatform("Windows 3.5"));
		assertEquals(Platform.OTHER, Platform.detectPlatform("Something else"));
		System.out.println("Current platform is: " + Platform.detectPlatform());

		assertEquals(Platform.OTHER, Platform.fromValue(1213321));
		assertEquals("other", Platform.OTHER.getLabel());
	}
}
