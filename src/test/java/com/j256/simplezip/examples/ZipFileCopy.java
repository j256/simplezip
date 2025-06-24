package com.j256.simplezip.examples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.ZipFileOutput;
import com.j256.simplezip.format.Zip64CentralDirectoryEnd;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo.Builder;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Copies a zip/jar/war file by reading it in with a ZipFileInput and writing it out with ZipFileOutput. With this
 * example you can see the mechanisms used to duplicate the Zip data while giving you full control of all fields.
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

		// open up our Zip data from the input file
		File outFile = new File(inFile.getPath() + ".out");
		long bytesRead = 0;
		long bytesWritten = 0;
		try (ZipFileInput zipInput = new ZipFileInput(inFile); //
				ZipFileOutput zipOutput = new ZipFileOutput(outFile);) {

			// copy all of the file entries from the input to the output
			copyFileEntries(zipInput, zipOutput);
			// assign the directory entries
			assignDirectoryFileInfo(zipInput, zipOutput);

			// extract our end information
			ZipCentralDirectoryEndInfo endInfo = extractEndInfo(zipInput);
			zipOutput.finishZip(endInfo);

			bytesRead = zipInput.getNumBytesRead();
			bytesWritten = zipOutput.getNumBytesWritten();
		}

		System.out.println("Read " + bytesRead + " bytes from " + inFile);
		System.out.println("Wrote " + bytesWritten + " bytes to " + outFile);
	}

	/**
	 * Read in each of the file-headers and associated file data and write them to the output.
	 */
	private void copyFileEntries(ZipFileInput zipInput, ZipFileOutput zipOutput) throws IOException {
		while (true) {
			// read in the next header
			ZipFileHeader fileHeader = zipInput.readFileHeader();
			if (fileHeader == null) {
				break;
			}

			// write out the header
			zipOutput.writeFileHeader(fileHeader);
			// copy raw bytes from the input stream to the output stream, raw mode so no encoding necessary
			try (InputStream fileDataInput = zipInput.openFileDataInputStream(true);
					OutputStream fileDataOutput = zipOutput.openFileDataOutputStream(true);) {
				IoUtils.copyStream(fileDataInput, fileDataOutput);
			}
		}
	}

	/**
	 * Read each of the central-directory file-entries and assign file-info to each of the files so that the same
	 * entries will be written to the output at the end of the Zip file.
	 */
	private void assignDirectoryFileInfo(ZipFileInput zipInput, ZipFileOutput zipOutput) throws IOException {
		while (true) {
			// read in the central-directory entries from in the input
			ZipCentralDirectoryFileEntry dirHeader = zipInput.readDirectoryFileEntry();
			if (dirHeader == null) {
				break;
			}

			// copy a couple of fields from the input central-directory entries to the output
			ZipCentralDirectoryFileInfo fileInfo =
					ZipCentralDirectoryFileInfo.Builder.fromCentralDirectoryFileEntry(dirHeader).build();
			zipOutput.addDirectoryFileInfo(dirHeader.getFileName(), fileInfo);
		}
	}

	/**
	 * Read the structures from the end of the Zip and extract fields in the end-info class so we can write the same
	 * structures in the output.
	 */
	private ZipCentralDirectoryEndInfo extractEndInfo(ZipFileInput zipInput) throws IOException {
		ZipCentralDirectoryEndInfo endInfo;
		Zip64CentralDirectoryEnd zip64End = zipInput.readZip64DirectoryEnd();
		if (zip64End == null) {
			ZipCentralDirectoryEnd zipEnd = zipInput.readDirectoryEnd();
			Builder endInfoBuilder = ZipCentralDirectoryEndInfo.Builder.fromCentralDirectoryEnd(zipEnd);
			endInfo = endInfoBuilder.build();
		} else {
			Builder endInfoBuilder = ZipCentralDirectoryEndInfo.Builder.fromCentralDirectoryEnd(zip64End);
			Zip64CentralDirectoryEndLocator locator = zipInput.readZip64DirectoryEndLocator();
			if (locator != null) {
				endInfoBuilder.setNumberDisks(locator.getNumberDisks());
			}
			endInfo = endInfoBuilder.build();
		}
		return endInfo;
	}
}
