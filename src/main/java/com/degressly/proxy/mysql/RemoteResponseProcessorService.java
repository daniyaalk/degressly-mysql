package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ResultSet;
import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
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

	public static final Map<Long, ResultSet> partialResults = new HashMap<>();

	public static final Map<Long, Boolean> awaitingHeaders = new HashMap<>();

	public static final Map<Long, Boolean> awaitingRows = new HashMap<>();

	@Autowired
	RemoteFieldDecoderFactory remoteFieldDecoderFactory;

	private void cleanUpAfterIngestingHeaders(long taskId, int lastColumnPacket) {
		awaitingHeaders.put(taskId, false);
		awaitingRows.put(taskId, true);
		partialResults.get(taskId).setPacketOffsetOfLastIngestedColumn(lastColumnPacket);
		// partialResults.remove(taskId);
	}

	public ResultSet processFirstPage(long id, List<MySQLPacket> packets) {
		if (!awaitingHeaders.getOrDefault(id, true)) {
			awaitingHeaders.putIfAbsent(id, true);
			return partialResults.get(id);
		}
		var resultSet = new ResultSet();
		partialResults.put(id, resultSet);

		if (packets.size() == 1 && Utils.isErrorPacket(packets.getFirst())) {
			return prepareErrorResultSet(id, packets, resultSet);
		}

		resultSet.setColumnCount(Utils.calculateIntLenEnc(packets.getFirst().getBody(), 0).getLeft());
		return resultSet;
	}

	private ResultSet prepareErrorResultSet(long id, List<MySQLPacket> packets, ResultSet resultSet) {
		var packet = packets.get(0);
		resultSet.setError(true);
		resultSet.setErrorCode(Arrays.copyOfRange(packet.getBody(), 1, 3));
		resultSet.setJdbcState(Arrays.copyOfRange(packet.getBody(), 3, 5));
		resultSet.setErrorMessage(
				(String) remoteFieldDecoderFactory.get(Encoding.STRING_NULL_TERMINATED).decode(packet, 5).getLeft());
		resultSet.setColumnCount(0);
		resultSet.setResultSetComplete(true);
		cleanUpAfterIngestingHeaders(id, 0);
		awaitingRows.put(id, false);
		return resultSet;
	}

	public ResultSet parseColumns(long id, List<MySQLPacket> packets, int packetNumber) {
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
				partialResultSet.setResultSetComplete(true);
				cleanUpAfterIngestingHeaders(id, i);
				break;
			}
		}
		return partialResultSet;
	}

	public ResultSet parseRows(long id, List<MySQLPacket> packets) {
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

			partialResult.getRowList().add(ResultSet.getRowFromPacket(packet, remoteFieldDecoderFactory));

		}

		if (partialResults.containsKey(id)) {
			partialResults.get(id).setPacketOffsetOfLastIngestedColumn(-1);
		}
		return partialResult;
	}

	private void cleanUpAfterIngestingRows(long taskId) {
		awaitingRows.put(taskId, false);
		awaitingHeaders.remove(taskId);
		partialResults.get(taskId).setResultSetComplete(true);
		partialResults.get(taskId).setPacketOffsetOfLastIngestedColumn(0);
		partialResults.remove(taskId);
	}

	private boolean areAllColumnsDefined(long id) {
		return partialResults.get(id).getColumnCount() == partialResults.get(id).getColumnList().size();
	}

}
