package com.degressly.proxy.mysql.decoders;

import com.degressly.proxy.dto.MySQLClientPacket;
import org.springframework.stereotype.Service;

@Service
public class MySQLClientPacketDecoder {

    public void process(MySQLClientPacket mySQLPacket, byte[] byteArray) {
        mySQLPacket.setHeader(MySQLHeaderDecoder.process(byteArray));
        processBody(mySQLPacket, byteArray);
    }

    private void processBody(MySQLClientPacket packet, byte[] byteArray) {
        var rawBody = new byte[byteArray.length-4];
        System.arraycopy(byteArray, 4, rawBody, 0, byteArray.length-4);
        var body = packet.getBody();
        body.setRaw(rawBody);
    }
}
