package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FilePermissionsTest {

	@Test
	public void testStuff() {
		assertEquals(0, FilePermissions.fromFile(new File("/doesnt_exist")));
		int dirFlags = (FilePermissions.MS_DOS_DIRECTORY | FilePermissions.UNIX_DIRECTORY);
		assertTrue((FilePermissions.fromFile(new File("target")) & dirFlags) == dirFlags);
	}
}
