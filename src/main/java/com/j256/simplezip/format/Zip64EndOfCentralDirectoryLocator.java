package com.j256.simplezip.format;

public class Zip64EndOfCentralDirectoryLocator {

	private static final int SIGNATURE = 0x7064b50;

	private int signature;
	private int diskNum;
	private long zip64EndOffset;
	private int totalDiskNum;
}
