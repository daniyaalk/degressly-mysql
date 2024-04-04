package com.degressly.proxy.dto;

import lombok.Data;

@Data
public class BaseMysqlHeader {
        byte[] raw;
        int packetBodyLength;
        long sequenceNumber;
}
