package com.degressly.proxy.dto.packet;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class MySQLPacket {
    private MySQLHeader header = new MySQLHeader();
    private byte[] body;
}

