package com.degressly.proxy.dto.actions.server;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultSet {

	private int columnCount;

	private List<Column> columnList = new ArrayList<>();

	// private List<>

	private boolean resultSetComplete;

	private int packetOffsetOfLastIngestedColumn = 0;

}
