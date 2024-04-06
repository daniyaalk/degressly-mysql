package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ResultSet;
import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ServerResponseProcessorService {

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
		resultSet.setColumnCount(Utils.calculateIntLenEnc(packets.getFirst().getBody(), 0).getLeft());
		return resultSet;
	}

	public ResultSet parseColumns(long id, List<MySQLPacket> packets, int packetNumber) {
		if (!awaitingHeaders.getOrDefault(id, true)) {
			return partialResults.get(id);
		}

		var partialResultSet = partialResults.get(id);
		for (int i = packetNumber; i < packets.size(); i++) {
			MySQLPacket packet = packets.get(i);

			// if (areAllColumnsDefined(id)) {
			// log.info("Processed columns: {}", partialResultSet);
			// partialResultSet.setResultSetComplete(true);
			// cleanUpAfterIngestingHeaders(id);
			// break;
			// }

			var column = new Column();
			populateColumnDefinition(packet, column);
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

		// // Need to skip first packet after header if it is EOF packet
		// int i = partialResult.getPacketOffsetOfLastIngestedColumn() + 1;
		// if (i < packets.size() && Utils.isEOFPacket(packets.get(i))) {
		// i++;
		// }

		for (int i = partialResult.getPacketOffsetOfLastIngestedColumn() + 1; i < packets.size(); i++) {
			var packet = packets.get(i);
			log.info("Packet for row parsing: {}", packet);
			if (Utils.isEOFPacket(packet)) {
				cleanUpAfterIngestingRows(id);
			}

		}

		return partialResult;
	}

	private void cleanUpAfterIngestingRows(long taskId) {
		awaitingRows.put(taskId, false);
		partialResults.get(taskId).setResultSetComplete(true);
		partialResults.get(taskId).setPacketOffsetOfLastIngestedColumn(0);
		partialResults.remove(taskId);
	}

	private void populateColumnDefinition(MySQLPacket packet, Column column) {
		Pair<Object, Integer> catalog = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTH_ENCODED).decode(packet, 0);
		column.setCatalog((String) catalog.getLeft());
		Pair<Object, Integer> schemaName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, catalog.getRight());
		column.setSchemaName((String) schemaName.getLeft());
		Pair<Object, Integer> tableName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, schemaName.getRight());
		column.setTableName((String) tableName.getLeft());
		Pair<Object, Integer> orgTableName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, tableName.getRight());
		column.setOrgColumnName((String) orgTableName.getLeft());
		Pair<Object, Integer> columnName = remoteFieldDecoderFactory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, orgTableName.getRight());
		column.setColumnName((String) columnName.getLeft());
	}

	private boolean areAllColumnsDefined(long id) {
		return partialResults.get(id).getColumnCount() == partialResults.get(id).getColumnList().size();
	}

}
