package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.actions.client.CommandCode;
import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import com.degressly.proxy.dto.actions.server.ResultSet;
import com.degressly.proxy.dto.packet.MySQLPacket;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

	private boolean awaitingResponseResultSet;

	private ResultSet partialResultSet = null;

	@Setter
	private Channel clientChannel;

	@Setter
	private Channel remoteChannel;

	@Autowired
	MySQLPacketDecoder packetDecoder;

	@Autowired
	RemoteResponseProcessorService remoteResponseProcessorService;

	public MySQLConnectionState(long id) {
		this.id = id;
	}

	public void processClientMessage(byte[] byteArray) {
		List<MySQLPacket> packets = packetDecoder.processMessage(byteArray, id);
		for (var packet : packets) {
			log.info("Client packet: {}", packet);
			log.info("Parsed packet body: {}", new String(packet.getBody()));

			if (isAuthDone) {
				MySQLClientAction action = convertToClientAction(packet);
				if (CommandCode.COM_QUERY.equals(action.getCommandCode())) {
					log.debug("awaiting response set to true");
					awaitingResponseResultSet = true;
				}
				log.info("Client action: {}", action);
			}

		}

	}

	public byte[] processRemoteMessage(byte[] byteArray) {
		List<MySQLPacket> packets = packetDecoder.processMessage(byteArray, id);

		for (var packet : packets) {
			log.info("Remote packet: {}", packet);
			log.info("Parsed packet body: {}", new String(packet.getBody()));
			if (!isAuthDone && packet.getHeader().getSequence() == 4 && packet.getBody()[3] == 0x02) {
				isAuthDone = true;
				log.info("Auth done");
			}
		}

		if (isAuthDone && awaitingResponseResultSet) {
			loadPartialResultSet(packets);
		}

		return byteArray;
	}

	private void loadPartialResultSet(List<MySQLPacket> packets) {
		if (Objects.isNull(partialResultSet)) {
			// First packet contains
			remoteResponseProcessorService.processFirstPage(id, packets);
			partialResultSet = remoteResponseProcessorService.parseColumns(id, packets, 1);
		}
		else {
			partialResultSet = remoteResponseProcessorService.parseColumns(id, packets, 0);
		}

		partialResultSet = remoteResponseProcessorService.parseRows(id, packets);

		log.info("{}", partialResultSet);

		if (partialResultSet.isResultSetComplete()) {
			partialResultSet = null;
			awaitingResponseResultSet = false;
		}
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
