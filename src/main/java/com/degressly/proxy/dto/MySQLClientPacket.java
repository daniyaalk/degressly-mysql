package com.degressly.proxy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MySQLClientPacket extends MySQLPacket{
    private BaseMysqlHeader header = new BaseMysqlHeader();
    private BaseMysqlBody body = new ClientMysqlBody();
}

