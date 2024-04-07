package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.TextFieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class Int2Enc implements TextFieldDecoder {

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
