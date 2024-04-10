package com.degressly.proxy.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HexFormat;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UtilsTest {

	@Test
	public void testGetByteArrayForIntLenEnc() {
		Assert.assertEquals("fc 00 00",
				HexFormat.of().withDelimiter(" ").formatHex(Utils.getByteArrayForIntLenEnc(251)));
		Assert.assertEquals("fc 1a 00",
				HexFormat.of().withDelimiter(" ").formatHex(Utils.getByteArrayForIntLenEnc(277)));

		System.out.println(HexFormat.of().withDelimiter(" ").formatHex(Utils.getByteArrayForIntLenEnc(65532)));

	}

}