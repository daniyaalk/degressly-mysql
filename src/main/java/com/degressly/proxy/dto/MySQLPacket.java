package com.degressly.proxy.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MySQLPacket {
    private BaseMysqlHeader header = new BaseMysqlHeader();
    private BaseMysqlBody body = new BaseMysqlBody();
}
