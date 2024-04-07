package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.TextFieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class Int4Enc implements TextFieldDecoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		int value = (packet.getBody()[offset] & 0xff) + ((packet.getBody()[offset + 1] & 0xff) << 8)
				+ ((packet.getBody()[offset + 2] & 0xff) << 16) + ((packet.getBody()[offset + 3] & 0xff) << 24);
		return Pair.of(value, offset + 4);
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_4;
	}

}
