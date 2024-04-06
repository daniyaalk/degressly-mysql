package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.dto.actions.server.parser.Encoding;
import com.degressly.proxy.dto.actions.server.parser.RemoteFieldDecoderFactory;
import com.degressly.proxy.dto.packet.MySQLPacket;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class Column {

	private String catalog;

	private String schemaName;

	private String tableName;

	private String orgTableName;

	private String columnName;

	private String orgColumnName;

	private int fixedFieldLength = 0x0c;

	private int charSet;

	private int columnLength;

	private FieldType type;

	private int flags;

	private int decimals;

	public static Column getColumnFromPacket(MySQLPacket packet, RemoteFieldDecoderFactory factory) {
		var column = new Column();
		Pair<Object, Integer> catalog = factory.get(Encoding.STRING_LENGTH_ENCODED).decode(packet, 0);
		column.setCatalog((String) catalog.getLeft());
		Pair<Object, Integer> schemaName = factory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, catalog.getRight());
		column.setSchemaName((String) schemaName.getLeft());
		Pair<Object, Integer> tableName = factory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, schemaName.getRight());
		column.setTableName((String) tableName.getLeft());
		Pair<Object, Integer> orgTableName = factory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, tableName.getRight());
		column.setOrgColumnName((String) orgTableName.getLeft());
		Pair<Object, Integer> columnName = factory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, orgTableName.getRight());
		column.setColumnName((String) columnName.getLeft());
		Pair<Object, Integer> orgColumnName = factory.get(Encoding.STRING_LENGTH_ENCODED)
			.decode(packet, columnName.getRight());
		column.setOrgColumnName((String) orgColumnName.getLeft());
		Pair<Object, Integer> fixedFieldLength = factory.get(Encoding.INT_LENGTH_ENCODED)
			.decode(packet, orgColumnName.getRight());
		column.setFixedFieldLength((int) fixedFieldLength.getLeft());
		Pair<Object, Integer> characterSet = factory.get(Encoding.INT_2).decode(packet, fixedFieldLength.getRight());
		column.setCharSet((int) characterSet.getLeft());
		Pair<Object, Integer> columnLength = factory.get(Encoding.INT_4).decode(packet, characterSet.getRight());
		column.setColumnLength((int) columnLength.getLeft());
		Pair<Object, Integer> type = factory.get(Encoding.INT_1).decode(packet, columnLength.getRight());
		column.setType(FieldType.fromValue((int) type.getLeft()));
		Pair<Object, Integer> flags = factory.get(Encoding.INT_2).decode(packet, type.getRight());
		column.setFlags((int) flags.getLeft());
		Pair<Object, Integer> decimals = factory.get(Encoding.INT_1).decode(packet, flags.getRight());
		column.setFlags((int) decimals.getLeft());
		return column;
	}

}
