package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ResultSet {

	private int columnCount;

	private List<Column> columnList = new ArrayList<>();

	private List<Map<Integer, String>> rowList = new ArrayList<>();

	private boolean resultSetComplete;

	private int packetOffsetOfLastIngestedColumn = -1;

	private boolean error;

	@Nullable
	private byte[] errorCode;

	@Nullable
	private byte[] jdbcState;

	@Nullable
	private String errorMessage;

	public static Map<Integer, String> getRowFromPacket(MySQLPacket packet,
			RemoteFieldDecoderFactory remoteFieldDecoderFactory) {
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

			Pair<Object, Integer> decidedStringOffsetPair = remoteFieldDecoderFactory
				.get(Encoding.STRING_LENGTH_ENCODED)
				.decode(packet, byteOffset);

			row.put(columnOffset, (String) decidedStringOffsetPair.getLeft());

			byteOffset += decidedStringOffsetPair.getRight();
			columnOffset++;
		}
		return row;
	}

}
