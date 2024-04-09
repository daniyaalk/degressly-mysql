package com.degressly.proxy.mysql;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ServerResponse;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.mysql.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RemoteResponseProcessorService {

	public static final Map<Long, ServerResponse> partialResults = new HashMap<>();

	public static final Map<Long, Boolean> awaitingHeaders = new HashMap<>();

	public static final Map<Long, Boolean> awaitingRows = new HashMap<>();

	@Autowired
	RemoteFieldDecoderFactory remoteFieldDecoderFactory;

	private void cleanUpAfterIngestingHeaders(long taskId, int lastColumnPacket) {
		awaitingHeaders.put(taskId, false);
		awaitingRows.put(taskId, true);
		partialResults.get(taskId).setPacketOffsetOfLastIngestedColumn(lastColumnPacket);
	}

	public ServerResponse processFirstPage(long id, List<MySQLPacket> packets) {
		if (!awaitingHeaders.getOrDefault(id, true)) {
			return partialResults.get(id);
		}
		var resultSet = new ServerResponse();
		partialResults.put(id, resultSet);

		if (packets.size() == 1 && Utils.isOKPacket(packets.getFirst())) {
			return prepareOKResultSet(id, packets, resultSet);
		}

		if (packets.size() == 1 && Utils.isErrorPacket(packets.getFirst())) {
			return prepareErrorResultSet(id, packets, resultSet);
		}

		resultSet.setColumnCount(Utils.calculateIntLenEnc(packets.getFirst().getBody(), 0).getLeft());
		return resultSet;
	}

	private ServerResponse prepareOKResultSet(long id, List<MySQLPacket> packets, ServerResponse serverResponse) {
		serverResponse.setOkPacket(true);
		var packet = packets.getFirst();
		serverResponse
			.setStatusMessage((String) remoteFieldDecoderFactory.getFieldDecoder(Encoding.STRING_LENGTH_ENCODED)
				.decode(packet, 5)
				.getLeft());
		serverResponse.setColumnCount(0);
		serverResponse.setResponseComplete(true);
		cleanUpAfterIngestingHeaders(id, 0);
		awaitingRows.put(id, false);
		return serverResponse;
	}

	private ServerResponse prepareErrorResultSet(long id, List<MySQLPacket> packets, ServerResponse serverResponse) {
		var packet = packets.getFirst();
		serverResponse.setError(true);
		serverResponse.setErrorCode(Arrays.copyOfRange(packet.getBody(), 1, 3));
		serverResponse.setJdbcState(Arrays.copyOfRange(packet.getBody(), 3, 5));
		serverResponse
			.setErrorMessage((String) remoteFieldDecoderFactory.getFieldDecoder(Encoding.STRING_NULL_TERMINATED)
				.decode(packet, 5)
				.getLeft());
		serverResponse.setColumnCount(0);
		serverResponse.setResponseComplete(true);
		awaitingHeaders.remove(id);
		awaitingRows.put(id, false);
		return serverResponse;
	}

	public ServerResponse processResponseForCOM_PREPARE(long id, List<MySQLPacket> packets) {
		var packet = packets.getFirst();
		ServerResponse serverResponse = new ServerResponse();

		serverResponse.setStatementId(
				(int) remoteFieldDecoderFactory.getFieldDecoder(Encoding.INT_4).decode(packet, 1).getLeft());
		serverResponse.setColumnCount(
				(int) remoteFieldDecoderFactory.getFieldDecoder(Encoding.INT_2).decode(packet, 5).getLeft());
		serverResponse.setResponseComplete(true);
		// serverResponse.setPacketOffsetOfLastIngestedColumn(0);
		serverResponse.setOkPacket(true);

		awaitingHeaders.remove(id);
		return serverResponse;
	}

	public ServerResponse parseColumnsForResultSet(long id, List<MySQLPacket> packets, int packetNumber) {
		if (!awaitingHeaders.getOrDefault(id, true)) {
			return partialResults.get(id);
		}

		var partialResultSet = partialResults.get(id);
		for (int i = packetNumber; i < packets.size(); i++) {
			MySQLPacket packet = packets.get(i);

			Column column = Column.getColumnFromPacket(packet, remoteFieldDecoderFactory);
			partialResultSet.getColumnList().add(column);

			if (areAllColumnsDefined(id)) {
				log.info("Processed columns: {}", partialResultSet);
				// partialResultSet.setResponseComplete(true);
				cleanUpAfterIngestingHeaders(id, i);
				break;
			}
		}
		return partialResultSet;
	}

	public ServerResponse parseRowsForCOM_QUERY(long id, List<MySQLPacket> packets) {
		var partialResult = partialResults.get(id);

		if (!awaitingRows.getOrDefault(id, false)) {
			return partialResult;
		}

		for (int i = partialResult.getPacketOffsetOfLastIngestedColumn() + 1; i < packets.size(); i++) {
			var packet = packets.get(i);
			log.info("Packet for row parsing: {}", packet);
			if (Utils.isEOFPacket(packet)) {
				cleanUpAfterIngestingRows(id);
				break;
			}

			partialResult.getRowList()
				.add(ServerResponse.getRowFromTextResultSetInPacket(packet, remoteFieldDecoderFactory));

		}

		if (partialResults.containsKey(id)) {
			partialResults.get(id).setPacketOffsetOfLastIngestedColumn(-1);
		}
		return partialResult;
	}

	public ServerResponse parseRowsForCOM_EXECUTE(long id, List<MySQLPacket> packets) {
		var partialResult = partialResults.get(id);

		if (!awaitingRows.getOrDefault(id, false)) {
			return partialResult;
		}

		for (int i = partialResult.getPacketOffsetOfLastIngestedColumn() + 1; i < packets.size(); i++) {
			var packet = packets.get(i);
			log.info("Packet for row parsing: {}", packet);
			if (Utils.isEOFPacket(packet)) {
				cleanUpAfterIngestingRows(id);
				break;
			}

			partialResult.getRowList()
				.add(ServerResponse.getRowFromBinaryResultSetInPacket(packet, partialResult,
						remoteFieldDecoderFactory));

		}

		if (partialResults.containsKey(id)) {
			partialResults.get(id).setPacketOffsetOfLastIngestedColumn(-1);
		}
		return partialResult;
	}

	private void cleanUpAfterIngestingRows(long taskId) {
		awaitingRows.remove(taskId);
		awaitingHeaders.remove(taskId);
		partialResults.get(taskId).setResponseComplete(true);
		partialResults.get(taskId).setPacketOffsetOfLastIngestedColumn(0);
		partialResults.remove(taskId);
	}

	private boolean areAllColumnsDefined(long id) {
		return partialResults.get(id).getColumnCount() == partialResults.get(id).getColumnList().size();
	}

}
