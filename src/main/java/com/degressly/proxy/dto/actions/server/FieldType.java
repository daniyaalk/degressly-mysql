package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.constants.Encoding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum FieldType {

	// TODO: Fix where encoding is null
	MYSQL_TYPE_DECIMAL(0, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_TINY(1, Encoding.INT_1),
	MYSQL_TYPE_SHORT(2, Encoding.INT_2), MYSQL_TYPE_LONG(3, Encoding.INT_4),
	MYSQL_TYPE_FLOAT(4, Encoding.STRING_VARIABLE_LENGTH, 4), MYSQL_TYPE_DOUBLE(5, Encoding.STRING_VARIABLE_LENGTH, 8),
	MYSQL_TYPE_NULL(6, Encoding.NULL), MYSQL_TYPE_TIMESTAMP(7, Encoding.DATE), MYSQL_TYPE_LONGLONG(8, Encoding.INT_8),
	MYSQL_TYPE_INT24(9, Encoding.INT_24), MYSQL_TYPE_DATE(10, Encoding.DATE), MYSQL_TYPE_TIME(11, Encoding.DATE),
	MYSQL_TYPE_DATETIME(12, Encoding.DATE), MYSQL_TYPE_YEAR(13, Encoding.INT_2), MYSQL_TYPE_NEWDATE(14, null),
	MYSQL_TYPE_VARCHAR(15, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_BIT(16, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_TIMESTAMP2(17, null), MYSQL_TYPE_DATETIME2(18, null), MYSQL_TYPE_TIME2(19, null),
	MYSQL_TYPE_TYPED_ARRAY(20, null), MYSQL_TYPE_INVALID(243, null), MYSQL_TYPE_BOOL(244, null),
	MYSQL_TYPE_JSON(245, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_NEWDECIMAL(246, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_ENUM(247, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_SET(248, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_TINY_BLOB(249, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_MEDIUM_BLOB(250, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_LONG_BLOB(251, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_BLOB(252, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_VAR_STRING(253, Encoding.STRING_LENGTH_ENCODED), MYSQL_TYPE_STRING(254, Encoding.STRING_LENGTH_ENCODED),
	MYSQL_TYPE_GEOMETRY(255, Encoding.STRING_LENGTH_ENCODED);

	private final int value;

	private final Encoding encoding;

	private int lengthArgument = 0;

	public static FieldType fromValue(int value) {
		for (var val : values()) {
			if (value == val.value) {
				return val;
			}
		}
		throw new IllegalArgumentException();
	}

}
