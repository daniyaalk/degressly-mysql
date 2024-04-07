package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.mysql.parser.RemoteTextFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ServerResponse {

	private int columnCount;

	private List<Column> columnList = new ArrayList<>();

	private List<Map<Integer, String>> rowList = new ArrayList<>();

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
	String statusMessage;

	public static Map<Integer, String> getRowFromTextResultSetInPacket(MySQLPacket packet,
																	   RemoteTextFieldDecoderFactory remoteTextFieldDecoderFactory) {
		Map<Integer, String> row = new HashMap<>();

		int byteOffset = 0, columnOffset = 0;

		while (byteOffset < packet.getBody().length) {

			// Check if field is empty, denoted by 0xfb
			// https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html
			if (packet.getBody().length == 1 && ((packet.getBody()[0] & 0xff) == 0xfb)) {
				row.put(columnOffset, null);
				byteOffset++;
				columnOffset++;
				continue;
			}

			Pair<Object, Integer> decidedStringOffsetPair = remoteTextFieldDecoderFactory
				.get(Encoding.STRING_LENGTH_ENCODED)
				.decode(packet, byteOffset);

			row.put(columnOffset, (String) decidedStringOffsetPair.getLeft());

			byteOffset += decidedStringOffsetPair.getRight();
			columnOffset++;
		}
		return row;
	}

	public static Map<Integer, String> getRowFromBinaryResultSetInPacket(MySQLPacket packet,
																	   RemoteTextFieldDecoderFactory remoteTextFieldDecoderFactory) {
		Map<Integer, String> row = new HashMap<>();


		return row;
	}

}
