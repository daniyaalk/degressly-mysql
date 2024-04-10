package com.degressly.proxy.utils;

import com.degressly.proxy.dto.packet.MySQLPacket;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@UtilityClass
public class Utils {

	private static final Map<Long, Integer> byteCountMap = Map.of(251L, 1, 252L, 2, 253L, 4, 254L, 8);

	public static Pair<Long, Integer> calculateIntLenEnc(byte[] bytes, int offset) {

		long lsb = bytes[offset] & 0xff;

		if (lsb < 251L) {
			return Pair.of(lsb, 1);
		}

		long finalValue = 0;

		for (int i = 0; i < byteCountMap.get(lsb); i++) {
			finalValue += ((bytes[offset + 1 + i] & 0xffL) << i * 8);
		}

		return Pair.of(finalValue, byteCountMap.get(lsb) + 1);
	}

	public static byte[] getByteArrayForIntLenEnc(long length) {
		// Need to fix this, not properly parsing large numbers here

		if (length < 251) {
			return new byte[] { (byte) (length & 0xff) };
		}
		else if (length < Math.pow(2, 16)) {
			return new byte[] { (byte) 0xfc, (byte) (length & 0xff), (byte) ((length >> 8) & 0xff) };
		}
		else if (length >= Math.pow(2, 16) && length < Math.pow(2, 24)) {
			return new byte[] { (byte) 0xfd, (byte) ((length >> 8) & 0xff), (byte) ((length >> 16) & 0xff) };
		}
		else {
			return new byte[] { (byte) 0xfe, (byte) ((length >> 8) & 0xff), (byte) ((length >> 16) & 0xff),
					(byte) ((length >> 24) & 0xff) };
		}
	}

	public byte[] getByteArrayForPacketSize(int length) {
		byte[] ret = new byte[3];
		ret[0] = (byte) (length & 0xff);
		ret[1] = (byte) ((length >> 8) & 0xff);
		ret[2] = (byte) ((length >> 16) & 0xff);

		return ret;
	}

	// public byte[] getDataFieldLengthBytes(int count) {
	// if (count >)
	// }

	public static boolean isEOFPacket(MySQLPacket packet) {
		// https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html
		return packet.getHeader().getBodyLength() < 9 && ((packet.getBody()[0] & 0xff) == 0xfe);
	}

	public static boolean isErrorPacket(MySQLPacket packet) {
		// https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html
		return ((packet.getBody()[0] & 0xff) == 0xff);
	}

	public static boolean isOKPacket(MySQLPacket packet) {
		https: // dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_ok_packet.html
		return packet.getBody().length > 7 && ((packet.getBody()[0] & 0xff) == 0x00)
				|| ((packet.getBody()[0] & 0xff) == 0xfe);
	}

	public static boolean checkIfFieldIsNullForBinaryResultSetRow(byte[] bitmap, int columnOffset) {
		int bytePosition = (columnOffset + 2) / 8;
		int bitPosition = (columnOffset + 2) % 8;

		return (bitmap[bytePosition] & (0x01 << bitPosition)) == 0x01;
	}

}
