package com.shadowbase.dto;

import java.util.List;

public record QueryAnalysis(
        String sql,
        List<String> tables,
        boolean parsed,
        String error
) {}