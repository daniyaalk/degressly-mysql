package com.degressly.proxy.mysql.parser;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;

public interface FieldDecoder {

	/**
	 * Decodes the field starting at given offset and returns a pair containing the value
	 * of the field and the beginning offset of next field
	 * @param packet Packet containing row
	 * @param offset Packet containing offset for start of field
	 * @return Pair.of(Value of field, offset for start of next field)
	 */
	Pair<Object, Integer> decode(MySQLPacket packet, int offset);

	default Pair<Object, Integer> decode(MySQLPacket packet, int offset, Object... args) {
		return decode(packet, offset);
	}

	Encoding getEncoding();

}
