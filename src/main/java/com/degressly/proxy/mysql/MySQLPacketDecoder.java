package com.degressly.proxy.mysql;

import com.degressly.proxy.dto.packet.MySQLHeader;
import com.degressly.proxy.dto.packet.MySQLPacket;
import com.degressly.proxy.exception.IncompletePacketException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MySQLPacketDecoder {

	private final Map<Long, byte[]> partialData = new HashMap<>();

	public List<MySQLPacket> processMessage(byte[] byteArray, long connectionId) {
		List<MySQLPacket> packetList = new ArrayList<>();
		byte[] newArray = Arrays.copyOf(byteArray, byteArray.length);

		if (partialData.containsKey(connectionId)) {
			newArray = ArrayUtils.addAll(partialData.get(connectionId), newArray);
			partialData.remove(connectionId);
		}

		int offset = 0;
		while (offset < newArray.length) {
			try {
				MySQLPacket MySQLPacket = new MySQLPacket();
				processPacket(MySQLPacket, newArray, offset);
				offset += MySQLPacket.getHeader().getBodyLength() + 4;
				packetList.add(MySQLPacket);
			}
			catch (IncompletePacketException e) {
				partialData.put(connectionId, Arrays.copyOfRange(newArray, offset, newArray.length));
				break;
			}
		}
		return packetList;
	}

	private void processPacket(MySQLPacket packet, byte[] byteArray, int offset) throws IncompletePacketException {
		if (offset + 4 > byteArray.length) {
			throw new IncompletePacketException();
		}
		packet.setHeader(processHeader(Arrays.copyOfRange(byteArray, offset, offset + 4)));
		if (offset + 4 + packet.getHeader().getBodyLength() > byteArray.length) {
			throw new IncompletePacketException();
		}
		processBody(packet, byteArray, offset);
	}

	private void processBody(MySQLPacket packet, byte[] byteArray, int offset) {
		var rawBody = new byte[packet.getHeader().getBodyLength()];
		System.arraycopy(byteArray, offset + 4, rawBody, 0, packet.getHeader().getBodyLength());
		packet.setBody(rawBody);
	}

	private static MySQLHeader processHeader(byte[] byteArray) {
		var rawHeader = new byte[4];
		System.arraycopy(byteArray, 0, rawHeader, 0, 4);

		var header = new MySQLHeader();
		int packetLength = ((rawHeader[0] & 0xff)) + ((rawHeader[1] & 0xff) << 8) + ((rawHeader[2] & 0xff) << 16);
		header.setBodyLength(packetLength);
		header.setSequence(rawHeader[3] & 0xff);
		header.setRaw(rawHeader);

		return header;
	}

}
