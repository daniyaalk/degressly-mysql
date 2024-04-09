package com.degressly.proxy.mysql;

import com.degressly.proxy.constants.CommandCode;
import com.degressly.proxy.constants.Encoding;
import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import com.degressly.proxy.dto.actions.server.Column;
import com.degressly.proxy.dto.actions.server.ServerResponse;
import com.degressly.proxy.mysql.parser.FieldEncoder;
import com.degressly.proxy.mysql.parser.RemoteFieldEncodeDecodeFactory;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		return switch (responseFor) {
			case COM_QUERY -> prepareByteArrayForTextResultSet(response, lastAction);
			case COM_EXECUTE -> prepareByteArrayForBinaryResultSet(response, lastAction);
			default -> null;
		};
	}

	private byte[] prepareByteArrayForTextResultSet(ServerResponse response, MySQLClientAction lastAction) {
		byte[] columnCountPacket = getColumnCountPacket(++lastAction.sequenceNumber, response.getColumnCount());
		byte[] columnDefinitions = getColumnDefinitionPackets(response);
		return ArrayUtils.addAll(columnCountPacket, columnDefinitions);
	}

	private byte[] getColumnDefinitionPackets(ServerResponse response) {

		List<Byte> byteList = new ArrayList<>();


		for (Column column: response.getColumnList()) {
			List<Byte> currentColumnDefinition = new ArrayList<>();
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getCatalog())));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getSchemaName())));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getTableName())));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(stringLenEncEncoder.encode(column.getOrgTableName())));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(intLenEncEncoder.encode(String.valueOf(column.getFixedFieldLength()))));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(int4ByteEncoder.encode(String.valueOf(column.getColumnLength()))));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(int1ByteEncoder.encode(String.valueOf(column.getFieldType().getValue()))));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(int2ByteEncoder.encode(String.valueOf(column.getFlags()))));
			currentColumnDefinition.addAll(primitiveArrayToObjectArray(int1ByteEncoder.encode(String.valueOf(column.getDecimals()))));
			byteList.addAll(currentColumnDefinition);
		}

		return new byte[0];
	}

	private byte[] prepareByteArrayForBinaryResultSet(ServerResponse response, MySQLClientAction lastAction) {
		return new byte[0];
	}

	private byte[] getColumnCountPacket(int sequenceNumber, int columnCount) {
		byte[] body = remoteFieldEncodeDecodeFactory.getFieldEncoder(Encoding.INT_LENGTH_ENCODED).encode(String.valueOf(columnCount));
		return createPacket(sequenceNumber, body);
	}

	private byte[] createPacket(int sequenceNumber, byte[] body) {
		byte nextSequence = (byte) ((sequenceNumber&0xff)%0xff);
		List<Byte> byteList = primitiveArrayToObjectArray(intLenEncEncoder.encode(String.valueOf(body.length)));
		while (byteList.size() < 3) {
			byteList.add((byte)0x00);
		}
		byteList.add(nextSequence);
		byteList.addAll(primitiveArrayToObjectArray(body));

		return objectArrayToPrimitive(byteList);
	}

	private static byte[] objectArrayToPrimitive(List<Byte> byteList) {
		byte[] bytes = new byte[byteList.size()];
		for (int i=0; i<byteList.size(); i++) {
			bytes[i] = byteList.get(i);
		}

		return bytes;
	}
	private static List<Byte> primitiveArrayToObjectArray(byte[] bytes) {
		return new ArrayList<>(Arrays.asList(ArrayUtils.toObject(bytes)));
	}

}
