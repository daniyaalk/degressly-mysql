package com.degressly.proxy.dto.actions.server.parser.impl;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.FieldDecoder;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class StringLenEnc implements FieldDecoder {
    @Override
    public Pair<Object, Integer> decode(MySQLPacket packet, int offset) {
        Pair<Integer, Integer> intLenEnc = Utils.calculateIntLenEnc(packet.getBody(), offset);
        var fieldLength = intLenEnc.getLeft();
        var sizeLength = intLenEnc.getRight();

        String decodedValue = new String(Arrays.copyOfRange(packet.getBody(), offset+sizeLength, offset+sizeLength+fieldLength));

        return Pair.of(decodedValue, offset+sizeLength+fieldLength);
    }

    @Override
    public Encoding getEncoding() {
        return Encoding.STRING_LENGTHENCODED;
    }
}
