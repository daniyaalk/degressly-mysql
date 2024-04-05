package com.degressly.proxy.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class MySQLClientPacket {
    private MySQLHeader header = new MySQLHeader();
    private ClientMySQLBody body = new ClientMySQLBody();
}

