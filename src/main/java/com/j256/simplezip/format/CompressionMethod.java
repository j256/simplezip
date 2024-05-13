package com.j256.simplezip.format;

/**
 * Compression method from the {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public enum CompressionMethod {

	NONE(0),
	SHRUNK(1),
	REDUCED_FACTOR_1(2),
	REDUCED_FACTOR_2(3),
	REDUCED_FACTOR_3(4),
	REDUCED_FACTOR_4(5),
	IMPLODED(6),
	TOKENIZING(7),
	DEFLATED(8),
	DEFLATED64(9),
	PKWARE_IMPLODED(10),
	PKWARE_RESERVED(11),
	BZIP2(12),
	RESERVED1(13),
	LZMA(14),
	RESERVED2(15),
	RESERVED3(16),
	RESERVED4(17),
	IBM_TERSE(18),
	IBM_LZ77(19),
	PPMD(99),
	UNKNOWN(-1),
	// end
	;

	private final int value;

	private CompressionMethod(int value) {
		this.value = value;
	}

	public static CompressionMethod fromValue(int value) {
		for (CompressionMethod method : values()) {
			if (method.value == value) {
				return method;
			}
		}
		return UNKNOWN;
	}

	public int getValue() {
		return value;
	}
}
