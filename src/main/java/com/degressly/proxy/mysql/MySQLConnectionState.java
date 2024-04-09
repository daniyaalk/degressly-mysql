package com.degressly.proxy.mysql;

import com.degressly.proxy.constants.CommandCode;
import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import com.degressly.proxy.dto.actions.server.PreparedStatementDto;
import com.degressly.proxy.dto.actions.server.ServerResponse;
import com.degressly.proxy.dto.packet.MySQLPacket;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Getter
@Component
@Scope("prototype")
public class MySQLConnectionState {

	private final long id;

	private boolean isAuthDone;

	private MySQLClientAction lastClientAction;

	private boolean awaitingResponseResultSet;

	private ServerResponse partialServerResponse = null;

	Map<Integer, PreparedStatementDto> preparedStatements = new HashMap<>();

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
				log.debug("MySQL Client action received: {}", action);
				if (CommandCode.COM_QUERY.equals(action.getCommandCode())
						|| CommandCode.COM_PREPARE.equals(action.getCommandCode())
						|| CommandCode.COM_EXECUTE.equals(action.getCommandCode())) {
					log.debug("awaiting response set to true");
					awaitingResponseResultSet = true;
				}
				lastClientAction = action;
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

		switch (lastClientAction.getCommandCode()) {
			case COM_QUERY, COM_EXECUTE -> loadPartialResultSetForQuery(packets);
			case COM_PREPARE -> loadPartialResultSetForCOM_PREPARE(packets);
		}
	}

	private void loadPartialResultSetForCOM_PREPARE(List<MySQLPacket> packets) {
		partialServerResponse = remoteResponseProcessorService.processResponseForCOM_PREPARE(id, packets);
		log.info("{}", partialServerResponse);

		if (partialServerResponse.isResponseComplete()) {
			storePreparedStatement(partialServerResponse);

			partialServerResponse = null;
			awaitingResponseResultSet = false;
		}
	}

	private void storePreparedStatement(ServerResponse partialServerResponse) {
		preparedStatements.put(partialServerResponse.getStatementId(),
				PreparedStatementDto.builder()
					.serverResponse(partialServerResponse)
					.lastClientAction(lastClientAction)
					.build());
	}

	private void loadPartialResultSetForQuery(List<MySQLPacket> packets) {
		if (Objects.isNull(partialServerResponse)) {
			// First packet contains
			remoteResponseProcessorService.processFirstPage(id, packets);
			partialServerResponse = remoteResponseProcessorService.parseColumnsForResultSet(id, packets, 1);
		}
		else {
			partialServerResponse = remoteResponseProcessorService.parseColumnsForResultSet(id, packets, 0);
		}

		partialServerResponse = remoteResponseProcessorService.parseRowsForResultSet(id, packets,
				lastClientAction.getCommandCode());

		log.info("{}", partialServerResponse);

		if (partialServerResponse.isResponseComplete()) {
			partialServerResponse = null;
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
			case COM_INIT_DB, COM_QUERY, COM_PREPARE ->
				new String(Arrays.copyOfRange(packet.getBody(), 1, packet.getBody().length));
			default -> null;
		};
	}

}
