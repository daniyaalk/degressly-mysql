package com.degressly.proxy.mysql;

import com.degressly.proxy.constants.CommandCode;
import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ServerResponse;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import com.degressly.proxy.mysql.parser.RemoteFieldEncodeDecodeFactory;
import com.degressly.proxy.utils.Utils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RemoteResponseEncoderService {

	@Autowired
	private RemoteFieldEncodeDecodeFactory remoteFieldEncodeDecodeFactory;

	private FieldEncoder stringLenEncEncoder;

	private FieldEncoder intLenEncEncoder;

	private FieldEncoder int4ByteEncoder;

	private FieldEncoder int2ByteEncoder;

	private FieldEncoder int1ByteEncoder;

	@PostConstruct
	public void init() {
		stringLenEncEncoder = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.STRING_LENGTH_ENCODED);
		intLenEncEncoder = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_LENGTH_ENCODED);
		int4ByteEncoder = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_4);
		int2ByteEncoder = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_2);
		int1ByteEncoder = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_1);
	}

	public byte[] encode(ServerResponse response, MySQLClientAction lastAction) {
		CommandCode responseFor = lastAction.getCommandCode();
		if (!response.isError()) {
			return switch (responseFor) {
				case COM_QUERY -> prepareByteArrayForTextResultSet(lastAction, response);
				case COM_EXECUTE -> prepareByteArrayForBinaryResultSet(response, lastAction);
				default -> null;
			};
		}
		return null;
	}

	private byte[] prepareByteArrayForTextResultSet(MySQLClientAction lastAction, ServerResponse response) {
		byte[] columnCountPacket = getColumnCountPacket(lastAction, response.getColumnCount());
		byte[] columnDefinitions = getColumnDefinitionPackets(lastAction, response);
		byte[] rowDefinitions = getTextRowDefinitions(lastAction, response);
		byte[] eofPacket = getEofPacket(lastAction, response);
		byte[] metadataArray = ArrayUtils.addAll(columnCountPacket, columnDefinitions);
		byte[] rowsDataArray = ArrayUtils.addAll(rowDefinitions, eofPacket);
		return ArrayUtils.addAll(metadataArray, rowsDataArray);
	}

	private byte[] getEofPacket(MySQLClientAction lastAction, ServerResponse response) {
		byte[] eofByteAndPacketWarningCount = ArrayUtils.addAll(
				new byte[]{(byte)0xfe},
				int2ByteEncoder.encode(String.valueOf(response.getNumberOfWarnings()))
		);
		byte[] body =
				ArrayUtils.addAll(
				eofByteAndPacketWarningCount,
				Objects.nonNull(response.getServerStatusBitmask())
				? response.getServerStatusBitmask() : new byte[] { 0x00, 0x00 }
		);
		// Adding phantom bytes here - need to understand why this is happening
		return createPacket(lastAction, ArrayUtils.addAll(body, new byte[]{0x00, 0x00}));
	}

	private byte[] getTextRowDefinitions(MySQLClientAction lastAction, ServerResponse response) {
		var rows = response.getRowList();
		List<Byte> rowDefinitionBytes = new ArrayList<>();

		for (Map<Integer, Object> row : rows) {
			List<Byte> rowBytes = new ArrayList<>();
			for (Map.Entry<Integer, Object> entry : row.entrySet()) {
				if (entry.getValue() == null) {
					rowBytes.add((byte) 0xfb);
					continue;
				}
				rowBytes.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode((String) entry.getValue())));
			}
			rowDefinitionBytes
				.addAll(primitiveArrayToObjectArray(createPacket(lastAction, objectArrayToPrimitive(rowBytes))));
		}

		return objectArrayToPrimitive(rowDefinitionBytes);
	}

	private byte[] getColumnDefinitionPackets(MySQLClientAction lastAction, ServerResponse response) {

		List<Byte> byteList = new ArrayList<>();

		for (Column column : response.getColumnList()) {
			List<Byte> currentColumnDefinition = new ArrayList<>();
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getCatalog())));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getSchemaName())));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getTableName())));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getOrgTableName())));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getColumnName())));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getOrgColumnName())));
			currentColumnDefinition.addAll(
					primitiveArrayToObjectArray(intLenEncEncoder.encode(String.valueOf(column.getFixedFieldLength()))));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(int2ByteEncoder.encode(String.valueOf(column.getCharSet()))));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(int4ByteEncoder.encode(String.valueOf(column.getColumnLength()))));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(
					int1ByteEncoder.encode(String.valueOf(column.getFieldType().getValue()))));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(int2ByteEncoder.encode(String.valueOf(column.getFlags()))));
			currentColumnDefinition
				.addAll(primitiveArrayToObjectArray(int1ByteEncoder.encode(String.valueOf(column.getDecimals()))));

			// So far samples from the server seem to have two extra null bytes at the end
			// of this column
			// Either this is a coincidence or I am missing something, this needs to be
			// looked into later.
			currentColumnDefinition.add((byte) 0x00);
			currentColumnDefinition.add((byte) 0x00);
			byteList.addAll(primitiveArrayToObjectArray(
					createPacket(lastAction, objectArrayToPrimitive(currentColumnDefinition))));
		}

		return objectArrayToPrimitive(byteList);
	}

	private byte[] prepareByteArrayForBinaryResultSet(ServerResponse response, MySQLClientAction lastAction) {
		return new byte[0];
	}

	private byte[] getColumnCountPacket(MySQLClientAction lastAction, int columnCount) {
		byte[] body = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_LENGTH_ENCODED)
			.encode(String.valueOf(columnCount));
		return createPacket(lastAction, body);
	}

	private byte[] createPacket(MySQLClientAction lastAction, byte[] body) {
		int sequenceNumber = lastAction.getSequenceNumber() + 1;
		lastAction.setSequenceNumber(sequenceNumber);
		byte nextSequence = (byte) ((sequenceNumber & 0xff) % 0xff);
		List<Byte> byteList = primitiveArrayToObjectArray(Utils.getByteArrayForPacketSize(body.length));
		byteList.add(nextSequence);
		byteList.addAll(primitiveArrayToObjectArray(body));

		return objectArrayToPrimitive(byteList);
	}

	private static byte[] objectArrayToPrimitive(List<Byte> byteList) {
		byte[] bytes = new byte[byteList.size()];
		for (int i = 0; i < byteList.size(); i++) {
			bytes[i] = byteList.get(i);
		}

		return bytes;
	}

	private static List<Byte> primitiveArrayToObjectArray(byte[] bytes) {
		return new ArrayList<>(Arrays.asList(ArrayUtils.toObject(bytes)));
	}

}
