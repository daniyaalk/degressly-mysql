package com.degressly.proxy.dto.actions.server.parser.impl;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class Int2Enc implements FieldDecoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		int value = (packet.getBody()[offset] & 0xff) + ((packet.getBody()[offset + 1] & 0xff) << 8);
		return Pair.of(value, offset + 2);
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_2;
	}

}
