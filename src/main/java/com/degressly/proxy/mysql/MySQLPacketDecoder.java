package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.MySQLPacket;

public abstract class MySQLPacketDecoder {
    public void process(MySQLPacket mySQLPacket, byte[] byteArray) {
        processHeaders(mySQLPacket, byteArray);
        processBody(mySQLPacket, byteArray);
    }

    protected void processHeaders(MySQLPacket packet, byte[] byteArray) {
        var rawHeader = new byte[4];
        System.arraycopy(byteArray, 0, rawHeader, 0, 4);

        var header = packet.getHeader();
        int packetLength = ((rawHeader[0] & 0xff)) + ((rawHeader[1] & 0xff) << 8) + ((rawHeader[2] & 0xff) << 16);
        header.setPacketBodyLength(packetLength);
        header.setSequenceNumber(rawHeader[3] & 0xff);
        header.setRaw(rawHeader);
    }

    protected void processBody(MySQLPacket packet, byte[] byteArray) {
        var rawBody = new byte[byteArray.length-4];
        System.arraycopy(byteArray, 4, rawBody, 0, byteArray.length-4);
        var body = packet.getBody();
        body.setRaw(rawBody);
    }
}
