package com.degressly.proxy.utils;

import com.degressly.proxy.dto.packet.MySQLPacket;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

@UtilityClass
public class Utils {

	public static Pair<Integer, Integer> calculateIntLenEnc(byte[] bytes, int offset) {
		int lsb = bytes[offset] & 0xff;

		return switch (lsb) {
			case 251 -> Pair.of(lsb + ((bytes[offset + 1] & 0xff) << 8), 2);
			case 252 -> Pair.of(lsb + ((bytes[offset + 1] & 0xff) << 8) + ((bytes[offset + 2] & 0xff) << 16), 3);
			case 253 -> Pair.of(lsb + ((bytes[offset + 1] & 0xff) << 8) + ((bytes[offset + 2] & 0xff) << 16)
					+ ((bytes[offset + 3] & 0xff) << 24), 4);
			default -> Pair.of(lsb, 1);
		};
	}

	public static byte[] getByteArrayForIntLenEnc(long length) {
		// Need to fix this, not properly parsing large numbers here

		if (length < 251) {
			return new byte[] { (byte) (length & 0xff) };
		}
		else if (length < Math.pow(2, 16)) {
			return new byte[] { (byte) 0xfc, (byte) ((length - 251) & 0xff), (byte) (((length - 251) >> 8) & 0xff) };
		}
		else if (length >= Math.pow(2, 16) && length < Math.pow(2, 24)) {
			return new byte[] { (byte) 0xfd, (byte) (((length - 252) >> 8) & 0xff),
					(byte) (((length - 252) >> 16) & 0xff) };
		}
		else {
			return new byte[] { (byte) 0xfe, (byte) (((length - 253) >> 8) & 0xff),
					(byte) (((length - 253) >> 16) & 0xff), (byte) ((length >> 24) & 0xff) };
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
