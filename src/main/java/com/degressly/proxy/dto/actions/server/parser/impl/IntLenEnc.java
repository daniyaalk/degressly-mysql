package com.degressly.proxy.dto.actions.server.parser.impl;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.FieldDecoder;
import com.degressly.proxy.dto.actions.server.parser.FieldEncoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class IntLenEnc implements FieldDecoder, FieldEncoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		Pair<Integer, Integer> intLengthSizePair = Utils.calculateIntLenEnc(packet.getBody(), offset);
		return Pair.of(intLengthSizePair.getLeft(), offset + intLengthSizePair.getRight());
	}

	@Override
	public byte[] encode(String value) {

		return new byte[0];
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_LENGTH_ENCODED;
	}

}
