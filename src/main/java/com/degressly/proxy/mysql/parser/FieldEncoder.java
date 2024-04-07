package com.degressly.proxy.mysql.parser;

import com.degressly.proxy.constants.Encoding;

public interface FieldEncoder {

	Encoding getEncoding();

	byte[] encode(String value);

}
