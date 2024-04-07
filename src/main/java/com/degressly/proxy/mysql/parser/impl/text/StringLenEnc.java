package com.degressly.proxy.mysql.parser.impl.text;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.TextFieldDecoder;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StringLenEnc implements TextFieldDecoder, FieldEncoder {

	@Override
	public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
		Pair<Integer, Integer> intLenEnc = Utils.calculateIntLenEnc(packet.getBody(), offset);
		var fieldLength = intLenEnc.getLeft();
		var sizeLength = intLenEnc.getRight();

		String decodedValue = new String(
				Arrays.copyOfRange(packet.getBody(), offset + sizeLength, offset + sizeLength + fieldLength));

		return Pair.of(decodedValue, offset + sizeLength + fieldLength);
	}

	@Override
	public byte[] encode(String value) {
		return new byte[0];
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.STRING_LENGTH_ENCODED;
	}

}
