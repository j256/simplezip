package com.j256.simplezip.format;

public class ArchiveExtraDataRecord {

	private static final int SIGNATURE = 0x8064b50;

	private int signature;
	private int length;
	private byte[] data;
}
