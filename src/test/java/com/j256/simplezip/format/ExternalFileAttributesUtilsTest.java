package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class ExternalFileAttributesUtilsTest {

	@Test
	public void testStuff() {
		assertEquals(0, ExternalFileAttributesUtils.fromFile(new File("/doesnt_exist")));
		int dirFlags = (ExternalFileAttributesUtils.MS_DOS_DIRECTORY | ExternalFileAttributesUtils.UNIX_DIRECTORY);
		assertTrue((ExternalFileAttributesUtils.fromFile(new File("target")) & dirFlags) == dirFlags);
	}
}
