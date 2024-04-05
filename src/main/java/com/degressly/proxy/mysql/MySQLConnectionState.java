package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.actions.client.CommandCode;
import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ResultSet;
import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
@Component
@Scope("prototype")
public class MySQLConnectionState {

    private final long id;

    private boolean isAuthDone;

    private boolean awaitingResponse;

    private ResultSet partialResultSet = null;

    @Setter
    private Channel clientChannel;

    @Setter
    private Channel remoteChannel;

    @Autowired
    MySQLPacketDecoder packetDecoder;

    @Autowired
    RemoteFieldDecoderFactory remoteFieldDecoderFactory;

    public MySQLConnectionState(long id) { this.id = id; }

    public void processClientMessage(byte[] byteArray) {
        List<MySQLPacket> packets = packetDecoder.processMessage(byteArray, id);
        for (var packet: packets) {
            log.info("Client packet: {}", packet);
            log.info("Parsed packet body: {}", new String(packet.getBody()));

            if (isAuthDone) {
                MySQLClientAction action = convertToClientAction(packet);
                if (CommandCode.COM_QUERY.equals(action.getCommandCode())) {
                    awaitingResponse = true;
                }
                log.info("Client action: {}", action);
            }

        }

    }

    public void processRemoteMessage(byte[] byteArray) {
        List<MySQLPacket> packets = packetDecoder.processMessage(byteArray, id);

        for (var packet: packets) {
            log.info("Remote packet: {}", packet);
            log.info("Parsed packet body: {}", new String(packet.getBody()));
            if (!isAuthDone && packet.getHeader().getSequence() == 4 && packet.getBody()[3] == 0x02) {
                isAuthDone = true;
                log.info("Auth done");
            } else if (isAuthDone && awaitingResponse) {
                if (Objects.isNull(partialResultSet)) {
                    processFirstPage(packets);
                    parseColumns(packets, 1);
                } else {
                    parseColumns(packets, 0);
                }
            }
        }
    }

    private void cleanUpRemoteResultSet() {
        partialResultSet = null;
        awaitingResponse = false;
    }

    private void processFirstPage(List<MySQLPacket> packets) {
        ResultSet resultSet = new ResultSet();
        this.partialResultSet = resultSet;
        resultSet.setColumnCount(packets.getFirst().getBody()[0]);
    }

    private void parseColumns(List<MySQLPacket> packets, int packetNumber) {
        for (int i = packetNumber; i <= partialResultSet.getColumnCount(); i++) {
            MySQLPacket packet = packets.get(i);

            if (Utils.isEOFPacket(packet) || areAllColumnsDefined()) {
                log.info("Processed columns: {}", partialResultSet);
                cleanUpRemoteResultSet();
                break;
            }

            var column = new Column();
            Pair<Object, Integer> catalog = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTHENCODED).decode(packet, 0);
            column.setCatalog((String) catalog.getLeft());
            Pair<Object, Integer> schemaName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTHENCODED).decode(packet, catalog.getRight());
            column.setSchemaName((String) schemaName.getLeft());
            Pair<Object, Integer> tableName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTHENCODED).decode(packet, schemaName.getRight());
            column.setTableName((String) tableName.getLeft());
            Pair<Object, Integer> orgTableName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTHENCODED).decode(packet, tableName.getRight());
            column.setOrgColumnName((String) orgTableName.getLeft());
            Pair<Object, Integer> columnName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTHENCODED).decode(packet, orgTableName.getRight());
            column.setColumnName((String) columnName.getLeft());
            partialResultSet.getColumnList().add(column);

            if (areAllColumnsDefined()) {
                log.info("Processed columns: {}", partialResultSet);
                cleanUpRemoteResultSet();
                break;
            }
        }

    }

    private boolean areAllColumnsDefined() {
        return partialResultSet.getColumnCount() == partialResultSet.getColumnList().size();
    }


    private MySQLClientAction convertToClientAction(MySQLPacket packet) {
        CommandCode commandCode = CommandCode.fromValue(packet.getBody()[0]);

        var action = new MySQLClientAction();
        action.setCommandCode(commandCode);
        action.setArgument(getClientArgument(packet, commandCode));
        return action;
    }

    private Object getClientArgument(MySQLPacket packet, CommandCode commandCode) {
        return switch (commandCode) {
            case COM_INIT_DB, COM_QUERY -> new String(Arrays.copyOfRange(packet.getBody(), 1, packet.getBody().length));
            default -> null;
        };
    }

}
