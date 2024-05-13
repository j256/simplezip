package com.j256.simplezip.format;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import com.j256.simplezip.format.ZipFileHeader.Builder;

public class ZipFileHeaderTest {

	@Test
	public void testDateTime() {
		LocalDateTime input;
		do {
			input = LocalDateTime.now();
			input = input.truncatedTo(ChronoUnit.SECONDS);
		} while (input.getSecond() % 2 != 0);

		Builder builder = ZipFileHeader.builder();
		builder.setLastModifiedDateTime(input);
		ZipFileHeader header = builder.build();

		LocalDateTime output = header.getLastModFileDateTime();
		assertEquals(input, output);
	}
}
