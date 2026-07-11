package com.shadowbase.dto;

public record DatabaseInfo(
        String id,
        String host,
        int port,
        String database,
        String username,
        String password,
        String jdbcUrl
) {}