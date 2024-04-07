package com.degressly.proxy.dto.actions.client;

import lombok.Getter;

@Getter
public enum CommandCode {

	COM_SLEEP(0x00), COM_INIT_DB(0x02), COM_QUERY(0x03), COM_FIELD_LIST(0x04), COM_QUIT(0x01), COM_PING(0x0e),
	COM_PREPARE(0x16), COM_EXECUTE(0x17), COM_LONG_DATA(0x18), COM_CLOSE_STMT(0x19), COM_RESET_STMT(0x1a)

	;

	private final int value;

	CommandCode(int value) {
		this.value = value;
	}

	public static CommandCode fromValue(int value) {
		for (CommandCode v : values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("No CommandCode Mapping found for " + value);
	}

}
