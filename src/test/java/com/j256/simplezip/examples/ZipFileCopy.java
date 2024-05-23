package com.j256.simplezip.examples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.ZipFileOutput;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Copies a zip file by reading it in with a ZipFileInput and writing it out with ZipFileOutput.
 * 
 * @author graywatson
 */
public class ZipFileCopy {

	public static void main(String[] args) throws IOException {
		new ZipFileCopy().doMain(args);
	}

	public void doMain(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: java ZipFileCopy zip-jar-or-war-file");
			System.exit(1);
			return;
		}
		File inFile = new File(args[0]);
		if (!inFile.exists()) {
			System.err.println("ERROR: file does not exist: " + inFile);
			System.exit(1);
			return;
		}

		// open up our zip file as a File
		ZipFileInput zipInput = new ZipFileInput(inFile);
		File outFile = new File(inFile.getPath() + ".out");
		ZipFileOutput zipOutput = new ZipFileOutput(outFile);
		copyFileEntries(zipInput, zipOutput);
		assignDirectoryFileInfo(zipInput, zipOutput);
		zipInput.readToEndOfZip();
		zipInput.close();
		zipOutput.close();
		System.out.println("Read " + zipInput.getNumBytesRead() + " bytes from " + inFile);
		System.out.println("Wrote " + zipOutput.getNumBytesWritten() + " bytes to " + outFile);
	}

	/**
	 * Display each of the headers with its information. It does not process any of the file data.
	 */
	private void copyFileEntries(ZipFileInput zipInput, ZipFileOutput zipOutput) throws IOException {
		while (true) {
			ZipFileHeader fileHeader = zipInput.readFileHeader();
			if (fileHeader == null) {
				break;
			}

			zipOutput.writeFileHeader(fileHeader);
			// copy raw bytes from the input to the output
			try (InputStream fileDataInput = zipInput.openFileDataInputStream(true);
					OutputStream fileDataOUtput = zipOutput.openFileDataOutputStream(true);) {
				IoUtils.copyStream(fileDataInput, fileDataOUtput);
			}
		}
	}

	private void assignDirectoryFileInfo(ZipFileInput zipInput, ZipFileOutput zipOutput) throws IOException {
		while (true) {
			ZipCentralDirectoryFileEntry dirHeader = zipInput.readDirectoryFileEntry();
			if (dirHeader == null) {
				break;
			}

			ZipCentralDirectoryFileInfo fileInfo =
					ZipCentralDirectoryFileInfo.Builder.fromCentralDirectoryFileEntry(dirHeader).build();
			zipOutput.addDirectoryFileInfo(dirHeader.getFileName(), fileInfo);
		}
	}
}
