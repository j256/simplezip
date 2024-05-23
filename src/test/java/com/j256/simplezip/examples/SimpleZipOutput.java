package com.j256.simplezip.examples;

import java.io.File;
import java.io.IOException;

import com.j256.simplezip.ZipFileOutput;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Simple zip output example program which writes 2 files and a directory into sample.zip.
 * 
 * @author graywatson
 */
public class SimpleZipOutput {

	public static void main(String[] args) throws IOException {

		File outFile = new File("sample.zip");
		ZipFileOutput zipOutput = new ZipFileOutput(outFile);
		// buffer the file data so we don't need to write data-descriptors
		zipOutput.enableBufferedOutput(Integer.MAX_VALUE, 1024 * 1024);

		// write our first file header
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName("hello.txt").build());

		byte[] messageBytes = "here's some text that we will write into the hello.txt file".getBytes();

		for (int i = 0; i < 10000; i++) {
			zipOutput.writeFileDataPart(messageBytes);
		}
		zipOutput.finishFileData();
		zipOutput.addDirectoryFileInfo(ZipCentralDirectoryFileInfo.builder()
				.withUnixExternalFileAttributes(0644)
				.withFileIsRegular(true)
				.withTextFile(true)
				.build());

		// here's another file (actually a directory) that we will write STORE'd as opposed to the default DEFLATE
		zipOutput.writeFileHeader(
				ZipFileHeader.builder().withFileName("dir/").withCompressionMethod(CompressionMethod.STORED).build());
		// no file data
		zipOutput.finishFileData();
		zipOutput.addDirectoryFileInfo(ZipCentralDirectoryFileInfo.builder()
				.withUnixExternalFileAttributes(0755)
				.withFileIsDirectory(true)
				.build());

		// write our last file header
		zipOutput.writeFileHeader(ZipFileHeader.builder().withFileName("dir/down.bin").build());

		byte[] bytes = new byte[] { 3, 12, 32, 31, 34, 24, 45, 45, 45, 4, 43, 34, 43, 43, 43, 43, };
		zipOutput.writeFileDataAll(bytes);
		zipOutput.addDirectoryFileInfo(ZipCentralDirectoryFileInfo.builder()
				.withUnixExternalFileAttributes(0644)
				.withFileIsRegular(true)
				.build());

		zipOutput.close();
		System.out.println("Wrote " + zipOutput.getNumBytesWritten() + " bytes to " + outFile);
	}
}
