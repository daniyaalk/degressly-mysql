package com.degressly.proxy.dto.actions.server.parser;

public interface FieldEncoder {

	Encoding getEncoding();

	byte[] encode(String value);

}
