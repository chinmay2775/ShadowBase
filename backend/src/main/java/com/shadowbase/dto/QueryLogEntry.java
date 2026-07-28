package com.shadowbase.dto;

import java.time.Instant;

public record QueryLogEntry(
        String databaseId,
        String sql,
        Instant timestamp,
        boolean success,
        String errorMessage
) {}