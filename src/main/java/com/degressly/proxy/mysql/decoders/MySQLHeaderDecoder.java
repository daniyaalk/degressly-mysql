package com.degressly.proxy.mysql.decoders;

import com.degressly.proxy.dto.MySQLHeader;

public class MySQLHeaderDecoder {

    public static MySQLHeader process(byte[] byteArray) {
        var rawHeader = new byte[4];
        System.arraycopy(byteArray, 0, rawHeader, 0, 4);

        var header = new MySQLHeader();
        int packetLength = ((rawHeader[0] & 0xff)) + ((rawHeader[1] & 0xff) << 8) + ((rawHeader[2] & 0xff) << 16);
        header.setBodyLength(packetLength);
        header.setSequence(rawHeader[3] & 0xff);
        header.setRaw(rawHeader);

        return header;
    }
}
