package com.j256.simplezip.format;

public class Zip64EndOfCentralDirectory {

	private static final int SIGNATURE = 0x6064b50;

	private int signature;
	private long sizeEnd;
	private int versionMade;
	private int versionNeeded;
	private int diskNumber;
	private int diskNumberStart;
	private long centralDirectoryOnDisk;
	private long entryNum;
	private long centralDirectorySize;
	private long centralDirectoryOffset;
	private byte[] extensibleData;
}
