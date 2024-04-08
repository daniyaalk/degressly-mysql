package com.degressly.proxy.constants;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum Encoding {

	INT_1, INT_2, INT_3, INT_4, INT_6, INT_8, INT_16, INT_24, INT_LENGTH_ENCODED,

	STRING_FIXED_LENGTH, STRING_NULL_TERMINATED, STRING_VARIABLE_LENGTH(true), STRING_LENGTH_ENCODED,
	STRING_REST_OF_PACKET,

	NULL, DATE

	;

	private boolean requiresLengthArgument;

	Encoding(boolean requiresLengthArgument) {
		this.requiresLengthArgument = requiresLengthArgument;
	}

}
