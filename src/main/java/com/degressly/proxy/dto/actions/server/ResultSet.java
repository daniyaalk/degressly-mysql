package com.degressly.proxy.dto.actions.server;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ResultSet {

	private int columnCount;

	private List<Column> columnList = new ArrayList<>();

	private List<Map<Integer, String>> rowList = new ArrayList<>();

	private boolean resultSetComplete;

	private int packetOffsetOfLastIngestedColumn = -1;

	private boolean error;

	@Nullable
	private byte[] errorCode;

	@Nullable
	private byte[] jdbcState;

	@Nullable
	private String errorMessage;

}
