package com.degressly.proxy.dto.actions.server.parser.impl;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import static com.degressly.proxy.dto.actions.server.parser.Encoding.STRING_NULL_TERMINATED;

@Component
public class StringNullTerminated implements FieldDecoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		var sb = new StringBuffer();

		while (offset < packet.getBody().length && ((packet.getBody()[offset] & 0xff) != 0x00)) {
			sb.append((char) packet.getBody()[offset]);
			offset++;
		}

		return Pair.of(sb.toString(), offset);
	}

	@Override
	public byte[] encode(String value) {
		return ArrayUtils.addAll(value.getBytes(), (byte) 0x00);
	}

	@Override
	public Encoding getEncoding() {
		return STRING_NULL_TERMINATED;
	}

}
