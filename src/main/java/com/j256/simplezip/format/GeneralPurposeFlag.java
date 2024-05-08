package com.j256.simplezip.format;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encoded into the {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public enum GeneralPurposeFlag {

	/**
	 * Sets whether or not the data-descriptor information follows the file data.
	 */
	ENCRYPTED(1 << 0),
	TYPE_6_IMPLODING_8K_BUFFER(1 << 1),
	TYPE_6_IMPLODING_3_SF_TREES(1 << 2),
	DEFLATING_NORMAL(0),
	DEFLATING_MAXIMUM(1),
	DEFLATING_FAST(2),
	DEFLATING_SUPER_FAST(3),
	DATA_DESCRIPTOR(1 << 3),
	ENHANCED_DEFLATING(1 << 4),
	COMPRESS_PATCHED(1 << 5),
	STRONG_COMPRESSION(1 << 6),
	UNUSED1(1 << 7),
	UNUSED2(1 << 8),
	UNUSED3(1 << 9),
	UNUSED4(1 << 10),
	LANGUAGE_ENCODING(1 << 11),
	PKWARE1(1 << 12),
	ENCRYPTED_CENTRAL(1 << 13),
	PKWARE2(1 << 14),
	PKWARE3(1 << 15),
	// end
	;

	private final int value;

	private GeneralPurposeFlag(int value) {
		this.value = value;
	}

	public static Set<GeneralPurposeFlag> fromInt(int value) {
		if (value == 0) {
			return Collections.emptySet();
		}
		Set<GeneralPurposeFlag> flags = new HashSet<>();
		for (GeneralPurposeFlag flag : values()) {
			if ((value & flag.value) > 0) {
				flags.add(flag);
			}
		}
		return flags;
	}

	public int getValue() {
		return value;
	}
}
