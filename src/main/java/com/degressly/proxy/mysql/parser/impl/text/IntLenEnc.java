package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.FieldDecoder;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class IntLenEnc implements FieldDecoder, FieldEncoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		Pair<Long, Integer> intLengthSizePair = Utils.calculateIntLenEnc(packet.getBody(), offset);
		return Pair.of(intLengthSizePair.getLeft(), offset + intLengthSizePair.getRight());
	}

	@Override
	public byte[] encode(String value) {
		long length = Long.parseUnsignedLong(value);
		return Utils.getByteArrayForIntLenEnc(length);
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_LENGTH_ENCODED;
	}

}
