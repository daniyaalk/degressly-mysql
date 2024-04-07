package com.degressly.proxy.dto.actions.client;

import com.degressly.proxy.constants.CommandCode;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class MySQLClientAction {

	CommandCode commandCode;

	@Nullable
	Object argument;

}
