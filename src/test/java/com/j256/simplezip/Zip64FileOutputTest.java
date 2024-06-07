package com.j256.simplezip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.j256.simplezip.format.Zip64CentralDirectoryEnd;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;

public class Zip64FileOutputTest {

	@Test
	public void testZip64End() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024000, 1024000);
		output.writeFileHeader(ZipFileHeader.builder().withFileName("foo.txt").build());
		output.writeFileDataAll(new byte[] { 1, 2, 3 });
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		builder.setVersionMade(2);
		output.addDirectoryFileInfo(builder.build());
		int versionMade = 1221;
		output.finishZip(ZipCentralDirectoryEndInfo.builder().withVersionMade(versionMade).build());
		output.close();

		byte[] zipBytes = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
		// now try to read it back in with the jdk stuff
		ZipFileInput input = new ZipFileInput(bais);
		assertNotNull(input.readFileHeader());
		input.readFileData(new ByteArrayOutputStream());
		assertNull(input.readFileHeader());
		ZipCentralDirectoryFileEntry entry = input.readDirectoryFileEntry();
		assertNotNull(entry);
		assertNull(input.readDirectoryFileEntry());
		Zip64CentralDirectoryEnd zip64End = input.readZip64DirectoryEnd();
		assertNotNull(zip64End);
		assertEquals(versionMade, zip64End.getVersionMade());
		Zip64CentralDirectoryEndLocator zip64EndLocator = input.readZip64DirectoryEndLocator();
		assertNotNull(zip64EndLocator);
		ZipCentralDirectoryEnd end = input.readDirectoryEnd();
		assertNotNull(end);
		input.close();
	}

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipFileOutput output = new ZipFileOutput(baos);
		output.enableFileBuffering(1024000, 1024000);
		output.writeFileHeader(ZipFileHeader.builder().withFileName("foo.txt").build());
		output.writeFileDataAll(new byte[] { 1, 2, 3 });
		ZipCentralDirectoryFileInfo.Builder builder = ZipCentralDirectoryFileInfo.builder();
		builder.setVersionMade(2);
		output.addDirectoryFileInfo(builder.build());
		int versionMade = 1221;
		output.finishZip(ZipCentralDirectoryEndInfo.builder().withVersionMade(versionMade).build());
		output.close();

		byte[] zipBytes = baos.toByteArray();
		FileOutputStream fos = new FileOutputStream("target/test64.zip");
		fos.write(zipBytes);
		fos.close();
	}
}
