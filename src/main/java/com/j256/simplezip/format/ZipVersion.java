package com.j256.simplezip.format;

/**
 * Encoded as "version needed by" in the files.
 * 
 * @author graywatson
 */
public enum ZipVersion {

	V1_0(1, 0),
	V1_1(1, 1),
	V2_0(2, 0),
	V2_1(2, 1),
	V2_5(2, 5),
	V2_7(2, 7),
	V4_5(4, 5),
	V4_6(4, 6),
	V5_0(5, 0),
	V5_1(5, 1),
	V5_2(5, 2),
	V6_0(6, 0),
	V6_1(6, 1),
	V6_2(6, 2),
	UNKNOWN(99, 99),
	// end
	;

	private static final ZipVersion DEFAULT_VERSION = V4_5;

	private final int major;
	private final int minor;
	private final int value;

	private ZipVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
		this.value = major * 10 + minor;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	/**
	 * Return the encoded byte value of this version.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Detect our platform by looking at various JDK attributes.
	 */
	public static ZipVersion detectPlatform() {
		// XXX no idea how to do this. can pick the lowest one based on how complicated the Zip features used
		return DEFAULT_VERSION;
	}

	/**
	 * Given an integer, return the associated platform..
	 */
	public static ZipVersion fromValue(int value) {
		for (ZipVersion version : values()) {
			if (version.value == value) {
				return version;
			}
		}
		return UNKNOWN;
	}
}
