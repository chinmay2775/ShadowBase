package com.shadowbase.dto;

import java.util.List;
import java.util.Map;

public record ProductionQueryResult(boolean success, List<String> columns, List<Map<String, Object>> rows, String errorMessage) {}