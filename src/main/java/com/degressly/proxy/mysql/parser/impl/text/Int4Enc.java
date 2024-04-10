package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class Int4Enc implements FieldDecoder, FieldEncoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		int value = (packet.getBody()[offset] & 0xff) + ((packet.getBody()[offset + 1] & 0xff) << 8)
				+ ((packet.getBody()[offset + 2] & 0xff) << 16) + ((packet.getBody()[offset + 3] & 0xff) << 24);
		return Pair.of(value, offset + 4);
	}

	@Override
	public byte[] encode(String value) {
		int number = Integer.parseInt(value);
		return new byte[] { (byte) (number & 0xff), (byte) ((number >> 8) & 0xff), (byte) ((number >> 16) & 0xff),
				(byte) ((number >> 24) & 0xff) };
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_4;
	}

}
