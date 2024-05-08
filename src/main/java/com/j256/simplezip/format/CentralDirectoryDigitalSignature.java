package com.j256.simplezip.format;

/**
 * File headers stored in the central directory.
 * 
 * @author graywatson
 */
public class CentralDirectoryDigitalSignature {

	private static final int SIGNATURE = 0x5054b50;

	private int signature;
	private int size;
	private byte[] data;
}
