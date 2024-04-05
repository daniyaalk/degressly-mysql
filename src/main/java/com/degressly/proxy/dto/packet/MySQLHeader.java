package com.degressly.proxy.dto.packet;

import lombok.Data;

@Data
public class MySQLHeader {
        private byte[] raw;
        private int bodyLength;
        private int sequence;
}
