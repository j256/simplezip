package com.j256.simplezip.format;

/**
 * File headers stored in the central directory.
 * 
 * @author graywatson
 */
public class CentralDirectoryFileHeader {

	/** signature that is expected to be at the start of the central directory */
	public static final int EXPECTED_SIGNATURE = 0x2014b50;

	private int signature;
	private int versionMade;
	private int versionNeeded;
	private int generalPurposeFlags;
	private int compressionMethod;
	private int lastModifiedFileTime;
	private int lastModifiedFileDate;
	private int crc32;
	private int compressedSize;
	private int uncompressedSize;
	private int fileNameLength;
	private int extraFieldLength;
	private int fileCommentLength;
	private int diskNumberStart;
	private int internalFileAttributes;
	private int externalFileAttributes;
	private int relativeOffsetOfLocalHeader;
	private String fileName;
	private byte[] extraField;
	private String comment;
}
