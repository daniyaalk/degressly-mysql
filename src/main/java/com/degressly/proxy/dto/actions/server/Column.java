package com.degressly.proxy.dto.actions.server;

import lombok.Data;

@Data
public class Column {
    private String catalog;
    private String schemaName;
    private String tableName;
    private String orgTableName;
    private String columnName;
    private String orgColumnName;
    private int fixedFieldLength = 0x0c;
    private int charSet;
    private int columnLength;
    private FieldType type;
    private int flags;
    private int decimals;
}
