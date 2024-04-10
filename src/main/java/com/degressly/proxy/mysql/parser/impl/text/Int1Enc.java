package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
public class Int1Enc implements FieldDecoder, FieldEncoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		return Pair.of((packet.getBody()[offset] & 0xff), offset + 1);
	}

	@Override
	public byte[] encode(String value) {
		int number = Integer.parseInt(value);
		return new byte[] { (byte) (number & 0xff) };
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_1;
	}

}
