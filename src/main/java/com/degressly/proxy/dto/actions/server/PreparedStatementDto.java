package com.degressly.proxy.dto.actions.server;

import com.degressly.proxy.dto.actions.client.MySQLClientAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreparedStatementDto {
    private final ServerResponse serverResponse;
    private final MySQLClientAction lastClientAction;
}
