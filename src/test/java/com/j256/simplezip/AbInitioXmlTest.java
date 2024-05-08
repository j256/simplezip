package com.j256.simplezip;

import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class AbInitioXmlTest {

	public static void main(String[] args) throws IOException {
		URL jarUrl = AbInitioXmlTest.class.getResource("/outer.jar");
		assertNotNull(jarUrl.openStream());
		URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl });
		assertNotNull(classLoader.findResource("foo.zip"));
		assertNotNull(classLoader.findResource("foo.zip!bar.xml"));
		classLoader.close();
	}

	@Test
	public void testStuff() throws IOException {
		FileOutputStream fos = new FileOutputStream("/tmp/outer.zip");
		String fileName = "foo.zip";
		ZipEntry zipEntry = new ZipEntry(fileName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.putNextEntry(zipEntry);
		inner(zos);
		zos.close();
		fos.close();
	}

	private void inner(ZipOutputStream outerZos) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(outerZos);
		String fileName = "bar.xml";
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		Random random = new Random();
		byte[] bytes = new byte[1024];
		random.nextBytes(bytes);
		zos.write(bytes);
		zos.closeEntry();
		zos.close();
	}
}
