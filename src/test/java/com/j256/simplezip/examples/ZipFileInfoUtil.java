package com.j256.simplezip.examples;

import java.io.File;
import java.io.IOException;

import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.format.ExternalFileAttributesUtils;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipDataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Script which displays information about a Zip file in excruciating detail.
 * 
 * @author graywatson
 */
public class ZipFileInfoUtil {

	private static final String INDENT = "   ";

	public static void main(String[] args) throws IOException {
		new ZipFileInfoUtil().doMain(args);
	}

	public void doMain(String[] args) throws IOException {

		if (args.length == 0) {
			System.err.println("No zip, jar, or war files specified on the command line");
			System.exit(1);
			return;
		}
		for (String arg : args) {
			File file = new File(arg);
			if (!file.exists()) {
				System.err.println("ERROR: file does not exist: " + file);
				continue;
			}

			// open up our zip file as a File
			try (ZipFileInput zipInput = new ZipFileInput(file);) {
				System.out.println("Information about " + file + ":");
				displayFileEntries(zipInput);
				displayCentralDirectoryFileEntries(zipInput);
				displayCentralDirectoryEnd(zipInput);
			}
		}
	}

	/**
	 * Display each of the headers with its information. It does not process any of the file data.
	 */
	private void displayFileEntries(ZipFileInput zipInput) throws IOException {
		while (true) {
			ZipFileHeader fileHeader = zipInput.readFileHeader();
			if (fileHeader == null) {
				break;
			}

			System.out.println(INDENT + "file-name: " + fileHeader.getFileName());
			System.out.println(INDENT + INDENT + "version-needed: "
					+ fileHeader.getZipVersionNeeded().getVersionString() + " (" + fileHeader.getVersionNeeded() + ")");
			System.out.println(INDENT + INDENT + "flags: " + fileHeader.getGeneralPurposeFlagsAsEnums() + " (0x"
					+ Integer.toHexString(fileHeader.getGeneralPurposeFlags()) + ")");
			System.out.println(INDENT + INDENT + "compression-method: " + fileHeader.getCompressionMethodAsEnum() + " ("
					+ fileHeader.getCompressionMethod() + ")");
			System.out.println(INDENT + INDENT + "last-modified: " + fileHeader.getLastModifiedDateTime() + " (date "
					+ fileHeader.getLastModifiedDate() + ", time " + fileHeader.getLastModifiedTime() + ")");
			System.out.println(INDENT + INDENT + "crc checksum: " + fileHeader.getCrc32());
			printSizes(INDENT + INDENT, fileHeader.getCompressedSize(), fileHeader.getUncompressedSize());
			System.out.println(INDENT + INDENT + "extra-bytes: " + fileHeader.getExtraFieldBytes().length);

			zipInput.skipFileData();
			ZipDataDescriptor dataDescriptor = zipInput.getCurrentDataDescriptor();
			if (dataDescriptor != null) {
				System.out.println(INDENT + INDENT + "data-descriptor:");
				System.out.println(INDENT + INDENT + INDENT + "crc checksum: " + dataDescriptor.getCrc32());
				printSizes(INDENT + INDENT + INDENT, dataDescriptor.getCompressedSize(),
						dataDescriptor.getUncompressedSize());
			}
		}
	}

	/**
	 * Display each of the central-directory file-entries.
	 */
	private void displayCentralDirectoryFileEntries(ZipFileInput zipInput) throws IOException {
		while (true) {
			ZipCentralDirectoryFileEntry dirEntry = zipInput.readDirectoryFileEntry();
			if (dirEntry == null) {
				break;
			}

			System.out.println(INDENT + "directory-entry: " + dirEntry.getFileName());
			System.out.println(INDENT + INDENT + "version-made: platform " + dirEntry.getPlatformMade() + ", version "
					+ dirEntry.getZipVersionMade().getVersionString() + " (" + dirEntry.getVersionMade() + ")");
			System.out.println(INDENT + INDENT + "version-needed: " + dirEntry.getZipVersionNeeded().getVersionString()
					+ " (" + dirEntry.getVersionNeeded() + ")");
			System.out.println(INDENT + INDENT + "flags: " + dirEntry.getGeneralPurposeFlagsAsEnums() + " (0x"
					+ Integer.toHexString(dirEntry.getGeneralPurposeFlags()) + ")");
			System.out.println(INDENT + INDENT + "compression-method: " + dirEntry.getCompressionMethodAsEnum() + " ("
					+ dirEntry.getCompressionMethod() + ")");
			System.out.println(INDENT + INDENT + "last-modified: " + dirEntry.getLastModifiedDateTime() + " (date "
					+ dirEntry.getLastModifiedDate() + ", time " + dirEntry.getLastModifiedTime() + ")");
			System.out.println(INDENT + INDENT + "crc checksum: " + dirEntry.getCrc32());
			printSizes(INDENT + INDENT, dirEntry.getCompressedSize(), dirEntry.getUncompressedSize());
			System.out.println(INDENT + INDENT + "disk number start: " + dirEntry.getDiskNumberStart());
			System.out.println(INDENT + INDENT + "internal file attributes: 0x"
					+ Integer.toHexString(dirEntry.getInternalFileAttributes()));
			System.out.println(INDENT + INDENT + "external file attributes: "
					+ ExternalFileAttributesUtils.toString(dirEntry.getExternalFileAttributes()) + " (0x"
					+ Integer.toHexString(dirEntry.getExternalFileAttributes()) + ")");
			System.out.println(
					INDENT + INDENT + "relative offset of local header: " + dirEntry.getRelativeOffsetOfLocalHeader());
			System.out.println(INDENT + INDENT + "extra-bytes: " + dirEntry.getExtraFieldBytes().length);
			System.out.println(INDENT + INDENT + "comment: " + dirEntry.getComment());
		}
	}

	private void displayCentralDirectoryEnd(ZipFileInput zipInput) throws IOException {
		ZipCentralDirectoryEnd dirEnd = zipInput.readDirectoryEnd();
		System.out.println(INDENT + "directory-end:");
		System.out.println(INDENT + INDENT + "disk number: " + dirEnd.getDiskNumber());
		System.out.println(INDENT + INDENT + "disk number start: " + dirEnd.getDiskNumberStart());
		System.out.println(INDENT + INDENT + "# records on disk: " + dirEnd.getNumRecordsOnDisk());
		System.out.println(INDENT + INDENT + "# records total: " + dirEnd.getNumRecordsTotal());
		System.out.println(INDENT + INDENT + "directory size: " + dirEnd.getDirectorySize());
		System.out.println(INDENT + INDENT + "directory offset: " + dirEnd.getDirectoryOffset());
		System.out.println(INDENT + INDENT + "comment: " + dirEnd.getComment());
	}

	private void printSizes(String indent, int compressed, int uncompressed) {
		float reduction = 0;
		if (uncompressed != 0) {
			reduction = Math.round(1000.0F * (1.0F - ((float) compressed / ((float) uncompressed)))) / 10.0F;
		}
		System.out.println(indent + "sizes: " + uncompressed + " uncompressed, " + compressed + " compressed ("
				+ reduction + "% reduction)");
	}
}
