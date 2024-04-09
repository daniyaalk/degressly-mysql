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
		Pair<Integer, Integer> intLengthSizePair = Utils.calculateIntLenEnc(packet.getBody(), offset);
		return Pair.of(intLengthSizePair.getLeft(), offset + intLengthSizePair.getRight());
	}

	@Override
	public byte[] encode(String value) {

		// Need to fix this, not properly parsing large numbers here

		long length = Long.parseUnsignedLong(value);
		if (length < 251) {
			return new byte[] { (byte) (length & 0xff) };
		}
		else if (length < Math.pow(2, 16)) {
			return new byte[] { (byte) 0xfc, (byte) (length & 0xff), (byte) ((length >> 8) & 0xff) };
		}
		else if (length >= Math.pow(2, 16) && length < Math.pow(2, 24)) {
			return new byte[] { (byte) 0xfd, (byte) ((length >> 8) & 0xff), (byte) ((length >> 16) & 0xff) };
		}
		else {
			return new byte[] { (byte) 0xfe, (byte) ((length >> 8) & 0xff), (byte) ((length >> 16) & 0xff),
					(byte) ((length >> 24) & 0xff) };
		}
	}

	@Override
	public Encoding getEncoding() {
		return Encoding.INT_LENGTH_ENCODED;
	}

}
