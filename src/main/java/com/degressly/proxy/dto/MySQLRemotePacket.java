package com.degressly.proxy.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class MySQLRemotePacket {
    private MySQLHeader header = new MySQLHeader();
    private RemoteMySQLBody body = new RemoteMySQLBody();
}
