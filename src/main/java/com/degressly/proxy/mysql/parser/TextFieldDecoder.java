package com.degressly.proxy.mysql.parser;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.packet.MySQLPacket;
import org.apache.commons.lang3.tuple.Pair;

public interface TextFieldDecoder {

	Pair<Object, Integer> decode(MySQLPacket packet, int offset);

	Encoding getEncoding();

}
