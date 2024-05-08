package com.j256.simplezip.format;

public class EndOfCentralDirectory {

	private static final int SIGNATURE = 0x6054b50;

	private int signature;
	private int diskNum;
	private int diskNumStartCentral;
	private int entryNum;
	private int size;
	private int offset;
	private int commentLength;
	private String comment;
}
