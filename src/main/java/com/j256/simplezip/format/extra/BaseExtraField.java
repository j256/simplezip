package com.j256.simplezip.format.extra;

import com.j256.simplezip.ZipStatus;

/**
 * Abstract class for all extra fields.
 * 
 * @author graywatson
 */
public abstract class BaseExtraField {

	private final int id;
	private final int extraSize;

	public BaseExtraField(int id, int extraSize) {
		this.id = id;
		this.extraSize = extraSize;
	}

	public int getId() {
		return id;
	}

	public int getExtraSize() {
		return extraSize;
	}

	/**
	 * Validate the field.
	 */
	public ZipStatus validate() {
		return ZipStatus.OK;
	}
}
