package com.degressly.proxy.dto.actions.server.parser.impl;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
public class Int1Enc implements FieldDecoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		return Pair.of((packet.getBody()[offset] & 0xff), offset + 1);
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_1;
	}

}
