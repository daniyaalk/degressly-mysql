package com.degressly.proxy.mysql.decoders;

import com.degressly.proxy.dto.MySQLRemotePacket;
import com.degressly.proxy.exception.IncompletePacketException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MySQLRemotePacketDecoder {

    private final Map<Long, byte[]> partialData = new HashMap<>();

    public List<MySQLRemotePacket> processMessage(byte[] byteArray, long connectionId) {
        List<MySQLRemotePacket> packetList = new ArrayList<>();

        if (partialData.containsKey(connectionId)) {
            byteArray = ArrayUtils.addAll(partialData.get(connectionId), byteArray);
        }

        int offset = 0;
        while(offset < byteArray.length) {
            try {
                MySQLRemotePacket mySQLRemotePacket = new MySQLRemotePacket();
                processPacket(mySQLRemotePacket, byteArray, offset);
                offset += mySQLRemotePacket.getHeader().getBodyLength()+4;
                packetList.add(mySQLRemotePacket);
            } catch (IncompletePacketException e) {
                partialData.put(connectionId, Arrays.copyOfRange(byteArray, offset, byteArray.length));
            }
        }
        return packetList;
    }

    private void processPacket(MySQLRemotePacket packet, byte[] byteArray, int offset) throws IncompletePacketException {
        if(offset+4 > byteArray.length) {
            throw new IncompletePacketException();
        }
        packet.setHeader(MySQLHeaderDecoder.process(Arrays.copyOfRange(byteArray, offset, offset+4)));
        if (offset+4+packet.getHeader().getBodyLength() > byteArray.length) {
            throw new IncompletePacketException();
        }
        processBody(packet, byteArray, offset);
    }


    private void processBody(MySQLRemotePacket packet, byte[] byteArray, int offset) {
        var rawBody = new byte[packet.getHeader().getBodyLength()];
        System.arraycopy(byteArray, offset+4, rawBody, 0, packet.getHeader().getBodyLength());
        var body = packet.getBody();
        body.setRaw(rawBody);
    }
}
