package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.FieldDecoder;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class StringNullTerminated implements FieldDecoder, FieldEncoder {

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
		return Encoding.STRING_NULL_TERMINATED;
	}

}
