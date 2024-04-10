package com.degressly.proxy.mysql.parser.impl.text;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Int2EncTest {

	@InjectMocks
	Int2Enc int2Enc;

	@Test
	public void encodingTest() {
		byte[] output = int2Enc.encode("3219");
		Assert.assertEquals("c", Integer.toHexString(output[1] & 0xff));
		Assert.assertEquals("93", Integer.toHexString(output[0] & 0xff));
	}

}