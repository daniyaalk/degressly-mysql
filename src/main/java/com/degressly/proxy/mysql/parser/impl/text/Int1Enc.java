package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.TextFieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
public class Int1Enc implements TextFieldDecoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		return Pair.of((packet.getBody()[offset] & 0xff), offset + 1);
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_1;
	}

}
