package com.j256.simplezip.format.extra;

import java.io.IOException;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Utility for reading in the extra fields.
 * 
 * @author graywatson
 */
public class ExtraFieldUtil {

	/**
	 * Read in an extra field returning either for a local file or the central directory.
	 */
	public <T extends BaseExtraField> T readExtraField(RewindableInputStream input, boolean fileHeader)
			throws IOException {
		/*
		 * WHen reading a file-header we aren't sure if this is a file-header or the start of the central directory.
		 */
		int id = IoUtils.readShort(input, "BaseExtraField.id");
		int size = IoUtils.readShort(input, "BaseExtraField.size");

		switch (id) {
			case ExtendedTimestampCentralExtraField.EXPECTED_ID: {
				if (fileHeader) {
					if (size == ExtendedTimestampLocalExtraField.EXPECTED_SIZE) {
						@SuppressWarnings("unchecked")
						T extra = (T) ExtendedTimestampLocalExtraField.read(input, id, size);
						return extra;
					}
				} else {
					if (size >= ExtendedTimestampCentralExtraField.EXPECTED_MINIMUM_SIZE) {
						@SuppressWarnings("unchecked")
						T extra = (T) ExtendedTimestampCentralExtraField.read(input, id, size);
						return extra;
					}
				}
			}
			case Unix1ExtraField.EXPECTED_ID: {
				if (size >= Unix1ExtraField.EXPECTED_MINIMUM_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Unix1ExtraField.read(input, id, size);
					return extra;
				}
			}
			case Unix2ExtraField.EXPECTED_ID: {
				if (size >= Unix2ExtraField.EXPECTED_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Unix2ExtraField.read(input, id, size);
					return extra;
				}
			}
			case Zip64ExtraField.EXPECTED_ID: {
				if (size == Zip64ExtraField.EXPECTED_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Zip64ExtraField.read(input, id, size);
					return extra;
				}
			}
			default: {
				// created below
				break;
			}
		}

		@SuppressWarnings("unchecked")
		T extra = (T) UnknownExtraField.read(input, id, size);
		return extra;
	}
}
