package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
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

	/**
	 * Write the start to the output-stream;
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeShort(outputStream, id);
		IoUtils.writeShort(outputStream, extraSize);
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
