package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.mysql.parser.RemoteFieldEncodeDecodeFactory;
import com.degressly.proxy.utils.Utils;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

import java.util.*;

@Data
public class ServerResponse {

	private int columnCount;

	private List<Column> columnList = new ArrayList<>();

	private List<Map<Integer, Object>> rowList = new ArrayList<>();

	private boolean responseComplete;

	private int packetOffsetOfLastIngestedColumn = -1;

	private boolean error;

	private boolean isOkPacket;

	private int statementId;

	private int numParams;

	@Nullable
	private byte[] errorCode;

	@Nullable
	private byte[] jdbcState;

	@Nullable
	private String errorMessage;

	@Nullable
	private String statusMessage;

	public static Map<Integer, Object> getRowFromTextResultSetInPacket(MySQLPacket packet,
			RemoteFieldEncodeDecodeFactory remoteFieldEncodeDecodeFactory) {
		Map<Integer, Object> row = new HashMap<>();

		int byteOffset = 0, columnOffset = 0;

		while (byteOffset < packet.getBody().length) {

			// Check if field is empty, denoted by 0xfb
			// https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html
			if ((packet.getBody()[byteOffset] & 0xff) == 0xfb) {
				row.put(columnOffset, null);
				byteOffset++;
				columnOffset++;
				continue;
			}

			Pair<Object, Integer> decodedStringAndNextOffset = remoteFieldEncodeDecodeFactory
				.getFieldDecoder(Encoding.STRING_LENGTH_ENCODED)
				.decode(packet, byteOffset);

			row.put(columnOffset, decodedStringAndNextOffset.getLeft());

			byteOffset = decodedStringAndNextOffset.getRight();
			columnOffset++;
		}
		return row;
	}

	public static Map<Integer, Object> getRowFromBinaryResultSetInPacket(MySQLPacket packet,
			ServerResponse partialResult, RemoteFieldEncodeDecodeFactory remoteFieldEncodeDecodeFactory) {
		Map<Integer, Object> row = new HashMap<>();

		// https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row_null_bitmap
		byte[] bitmap = Arrays.copyOfRange(packet.getBody(), 0, (partialResult.getColumnCount() + 9) / 8);

		int byteOffset = bitmap.length + 1, columnOffset = 0;

		while (byteOffset < packet.getBody().length) {
			if (Utils.checkIfFieldIsNullForBinaryResultSetRow(bitmap, columnOffset)) {
				row.put(columnOffset, null);
				columnOffset++;
				continue;
			}

			FieldType fieldType = partialResult.getColumnList().get(columnOffset).getFieldType();

			Pair<Object, Integer> decodedValueAndNextOffsetAddition = remoteFieldEncodeDecodeFactory
				.getFieldDecoder(fieldType.getEncoding())
				.decode(packet, byteOffset);
			row.put(columnOffset, decodedValueAndNextOffsetAddition.getLeft());

			byteOffset = decodedValueAndNextOffsetAddition.getRight();
			columnOffset++;
		}

		return row;
	}

}
