package com.j256.simplezip.format;

/**
 * Encoded as "version needed by" in the files.
 * 
 * @author graywatson
 */
public enum Version {

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
	// end
	;

	private final int major;
	private final int minor;

	private Version(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}
}
