package com.j256.simplezip.format;

public enum InternalFileAttributes {

	PKWARE1(1 << 0),
	PKWARE2(1 << 0),
	// end
	;

	private final int value;

	private InternalFileAttributes(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
