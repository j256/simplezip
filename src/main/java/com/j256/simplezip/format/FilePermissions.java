package com.j256.simplezip.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that sets file permissions to be assigned to the {@link CentralDirectoryFileHeader}.
 * 
 * @author graywatson
 */
public class FilePermissions {

	private static int DEFAULT_READ_ONLY_PERMISSIONS = 0444;
	private static int DEFAULT_READ_WRITE_PERMISSIONS = 0644;
	private static int DEFAULT_READ_ONLY_EXECUTE_PERMISSIONS = 0555;
	private static int DEFAULT_READ_WRITE_EXECUTE_PERMISSIONS = 0755;

	public int fileToPermissions(File file) {
		if (!file.exists()) {
			return 0;
		}
		int permissions = 0;
		try {
			Path path = FileSystems.getDefault().getPath(file.getPath());
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
			permissions = Permission.modeFromPermSet(perms);
		} catch (Exception e) {
			// non-posix
			if (file.canRead()) {
			}
			if (file.canExecute()) {
				if (file.canWrite()) {
					return DEFAULT_READ_WRITE_EXECUTE_PERMISSIONS;
				} else {
					return DEFAULT_READ_ONLY_EXECUTE_PERMISSIONS;
				}
			} else if (file.canWrite()) {
				return DEFAULT_READ_WRITE_PERMISSIONS;
			} else {
				return DEFAULT_READ_ONLY_PERMISSIONS;
			}
		}
		return permissions;
	}

	/**
	 * Returns true if the file is a symlink otherwise false.
	 */
	private static boolean isSymlink(File file) throws IOException {
		File canonFile;
		File canonDir = file.getParentFile();
		if (canonDir == null) {
			canonFile = file;
		} else {
			canonFile = new File(canonDir.getCanonicalFile(), file.getName());
		}
		return !canonFile.getCanonicalFile().equals(canonFile.getAbsoluteFile());
	}

	/**
	 * Mapping from the posix permissions to tar file modes.
	 */
	public static enum Permission {

		OWNER_READ(PosixFilePermission.OWNER_READ, 0400),
		OWNER_WRITE(PosixFilePermission.OWNER_WRITE, 0200),
		OWNER_EXECUTE(PosixFilePermission.OWNER_EXECUTE, 0100),
		GROUP_READ(PosixFilePermission.GROUP_READ, 0040),
		GROUP_WRITE(PosixFilePermission.GROUP_WRITE, 0020),
		GROUP_EXECUTE(PosixFilePermission.GROUP_EXECUTE, 0010),
		OTHERS_READ(PosixFilePermission.OTHERS_READ, 0004),
		OTHERS_WRITE(PosixFilePermission.OTHERS_WRITE, 0002),
		OTHERS_EXECUTE(PosixFilePermission.OTHERS_EXECUTE, 0001),
		// end
		;

		private final PosixFilePermission permission;
		private final int value;
		private static final Map<PosixFilePermission, Integer> posixPermValueMap = new HashMap<>();

		static {
			for (Permission permValue : values()) {
				posixPermValueMap.put(permValue.permission, permValue.value);
			}
		}

		private Permission(PosixFilePermission permission, int value) {
			this.permission = permission;
			this.value = value;
		}

		/**
		 * Get a mode int from a collection of permissions.
		 */
		public static int modeFromPermSet(Collection<PosixFilePermission> permSet) {
			int mode = 0;
			for (PosixFilePermission perm : permSet) {
				Integer permValue = posixPermValueMap.get(perm);
				if (permValue != null) {
					mode |= permValue;
				}
			}
			return mode;
		}

		/**
		 * Get a collection of permissions based from the mode.
		 */
		public static Set<PosixFilePermission> permSetFromMode(int mode) {
			Set<PosixFilePermission> permSet = new HashSet<>(Permission.values().length);
			for (Permission permValue : Permission.values()) {
				if ((mode & permValue.value) > 0) {
					permSet.add(permValue.permission);
				}
			}
			return permSet;
		}
	}
}
