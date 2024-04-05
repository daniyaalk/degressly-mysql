package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.MySQLClientPacket;
import com.degressly.proxy.dto.MySQLRemotePacket;
import com.degressly.proxy.mysql.decoders.MySQLClientPacketDecoder;
import com.degressly.proxy.mysql.decoders.MySQLRemotePacketDecoder;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Getter
@Component
@Scope("prototype")
public class MySQLConnectionState {

    private final long id;

    private boolean isAuthDone = false;

    @Setter
    private Channel clientChannel;

    @Setter
    private Channel remoteChannel;

    @Autowired
    MySQLRemotePacketDecoder remotePacketDecoder;

    @Autowired
    MySQLClientPacketDecoder clientPacketDecoder;

    public MySQLConnectionState(long id) { this.id = id; }

    public void processClientMessage(byte[] byteArray) {
        var packet = new MySQLClientPacket();
        clientPacketDecoder.process(packet, byteArray);
        log.info("Client packet: {}", packet);
        log.info("Parsed packet body: {}", new String(packet.getBody().getRaw()));
    }

    public void processRemoteMessage(byte[] byteArray) {
        List<MySQLRemotePacket> packets = remotePacketDecoder.processMessage(byteArray, id);

        for (MySQLRemotePacket packet: packets) {
            log.info("Remote packet: {}", packet);
            log.info("Parsed packet body: {}", new String(packet.getBody().getRaw()));
            if (!isAuthDone && packet.getHeader().getSequence() == 4 && packet.getBody().getRaw()[3] == 0x02) {
                isAuthDone = true;
                log.info("Auth done");
            }
        }
    }
}
