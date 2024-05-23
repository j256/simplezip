package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExternalFileAttributesUtilsTest {

	@Test
	public void testStuff() {
		assertEquals(0, ExternalFileAttributesUtils.fromFile(new File("/doesnt_exist")));
		int dirFlags = (ExternalFileAttributesUtils.MS_DOS_DIRECTORY | ExternalFileAttributesUtils.UNIX_DIRECTORY);
		assertTrue((ExternalFileAttributesUtils.fromFile(new File("target")) & dirFlags) == dirFlags);
	}

	@Test
	public void testJavaPermissions() {
		assertEquals(0, ExternalFileAttributesUtils.fromFile(new File("/doesnt_exist"), true));
		int dirFlags = (ExternalFileAttributesUtils.MS_DOS_DIRECTORY | ExternalFileAttributesUtils.UNIX_DIRECTORY);
		assertTrue((ExternalFileAttributesUtils.fromFile(new File("target"), true) & dirFlags) == dirFlags);
	}

	@Test
	public void testPermissionsString() {
		int permissions = ExternalFileAttributesUtils.MS_DOS_DIRECTORY;
		assertEquals("ms-dos: directory", ExternalFileAttributesUtils.toString(permissions));
		permissions = ExternalFileAttributesUtils.MS_DOS_READONLY;
		assertEquals("ms-dos: read-only", ExternalFileAttributesUtils.toString(permissions));
		permissions = ExternalFileAttributesUtils.UNIX_DIRECTORY;
		assertEquals("unix: 040000", ExternalFileAttributesUtils.toString(permissions));
		permissions = ExternalFileAttributesUtils.MS_DOS_READONLY;
		assertEquals("ms-dos: read-only", ExternalFileAttributesUtils.toString(permissions));
		permissions = ExternalFileAttributesUtils.MS_DOS_READONLY
				| ExternalFileAttributesUtils.UNIX_READ_ONLY_EXECUTE_PERMISSIONS;
		assertEquals("ms-dos: read-only, unix: 0555", ExternalFileAttributesUtils.toString(permissions));
	}

	@Test
	public void testAssignToUnknownFile() {
		ExternalFileAttributesUtils.assignToFile(new File("/doesnt_exist"), 12);
	}

	@Test
	public void testFromFile() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".t");
		file.deleteOnExit();
		file.setReadable(false, false);
		file.setWritable(false, false);
		file.setExecutable(false, false);
		file.setReadable(true, false);
		assertEquals("ms-dos: read-only, unix: 0100444",
				ExternalFileAttributesUtils.toString(ExternalFileAttributesUtils.fromFile(file, true)));
		file.setWritable(true, false);
		assertEquals("unix: 0100644",
				ExternalFileAttributesUtils.toString(ExternalFileAttributesUtils.fromFile(file, true)));
		file.setExecutable(true, false);
		assertEquals("unix: 0100755",
				ExternalFileAttributesUtils.toString(ExternalFileAttributesUtils.fromFile(file, true)));
		file.setWritable(false, false);
		assertEquals("ms-dos: read-only, unix: 0100555",
				ExternalFileAttributesUtils.toString(ExternalFileAttributesUtils.fromFile(file, true)));
		file.delete();
	}
}
