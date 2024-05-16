package com.j256.simplezip.format;

/**
 * Encoded as "version made by" in the {@link CentralDirectoryFileHeader#getVersionMade()}.
 * 
 * @author graywatson
 */
public enum Platform {

	MSDOS_AND_OS2(0),
	AMIGA(1),
	OPENVMS(2),
	UNIX(3),
	VM_CMS(4),
	ATARI_ST(5),
	OS2_HPFS(6),
	MACINTOSH(7),
	Z_SYSTEM(8),
	CPM(9),
	WINDOWS(10),
	MVS(11),
	VSE(12),
	ACORD(13),
	VFAT(14),
	ALT_MVS(15),
	BEOS(16),
	TANDEM(17),
	OS400(18),
	OSX(19),
	OTHER(-1),
	// end
	;

	// XXX: prolly need string versions
	private final int value;

	private Platform(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Detect our platform by looking at various JDK attributes.
	 */
	public static Platform detectPlatform() {
		String os = System.getProperty("os.name");
		if (os.contains("OS X")) {
			return MACINTOSH;
		}
		return OTHER;
	}

	/**
	 * Given an integer, return the associated platform..
	 */
	public static Platform fromValue(int value) {
		for (Platform platform : values()) {
			if (platform.value == value) {
				return platform;
			}
		}
		return OTHER;
	}
}
