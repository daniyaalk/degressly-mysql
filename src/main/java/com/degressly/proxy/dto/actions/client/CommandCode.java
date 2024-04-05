package com.degressly.proxy.dto.actions.client;

import lombok.Getter;

@Getter
public enum CommandCode {
    COM_SLEEP(0x00, null),
    COM_INIT_DB(0x02, null),
    COM_QUERY(0x03, String.class)


    ;
    private final int value;
    private final Class<?> clazz;
    CommandCode(int value, Class<?> clazz) {
        this.value = value;
        this.clazz = clazz;
    }
    public static CommandCode fromValue(int value) {
        for (CommandCode v: values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
