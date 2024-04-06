package com.degressly.proxy.dto.actions.server.parser;

import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;

public interface FieldDecoder {

	Pair<Object, Integer> decode(MySQLPacket packet, int offset);

	Encoding getEncoding();

}
