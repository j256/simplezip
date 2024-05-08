package com.j256.simplezip.format;

public class Zip64ExtendedInfiormation {

	private int type;
	private int size;
	private long originalSize;
	private long compressedSize;
	private long offset;
	private int diskNumber;
	private byte[] data;
}
