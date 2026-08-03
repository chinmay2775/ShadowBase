package com.shadowbase.dto;

import java.util.List;

public record SchemaCheckResult(
        String sql,
        List<String> tables,
        List<String> referencedColumns,
        boolean safe,
        List<String> issues
) {}