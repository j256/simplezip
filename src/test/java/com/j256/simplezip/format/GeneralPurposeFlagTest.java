package com.j256.simplezip.format;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeneralPurposeFlagTest {

	@Test
	public void testStuff() {
		assertTrue(GeneralPurposeFlag.fromInt(0).contains(GeneralPurposeFlag.DEFLATING_NORMAL));

		GeneralPurposeFlag flag = GeneralPurposeFlag.DEFLATING_NORMAL;
		assertTrue(GeneralPurposeFlag.fromInt(flag.getValue()).contains(flag));
		flag = GeneralPurposeFlag.DEFLATING_FAST;
		assertTrue(GeneralPurposeFlag.fromInt(flag.getValue()).contains(flag));
		flag = GeneralPurposeFlag.DEFLATING_SUPER_FAST;
		assertTrue(GeneralPurposeFlag.fromInt(flag.getValue()).contains(flag));
		flag = GeneralPurposeFlag.DEFLATING_MAXIMUM;
		assertTrue(GeneralPurposeFlag.fromInt(flag.getValue()).contains(flag));
	}

}
