package com.degressly.proxy.dto;

import lombok.Data;

@Data
public class MySQLHeader {
        private byte[] raw;
        private int bodyLength;
        private int sequence;
}
