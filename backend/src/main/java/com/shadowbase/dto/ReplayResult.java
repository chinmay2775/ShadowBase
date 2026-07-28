package com.shadowbase.dto;

public record ReplayResult(
        String sql,
        boolean success,
        String errorMessage
) {}