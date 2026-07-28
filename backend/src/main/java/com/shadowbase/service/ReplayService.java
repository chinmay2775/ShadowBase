package com.shadowbase.service;

import com.shadowbase.dto.QueryLogEntry;
import com.shadowbase.dto.ReplayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ReplayService {

    private static final Logger log = LoggerFactory.getLogger(ReplayService.class);

    private final TrafficLogService trafficLogService;
    private final DatabaseService databaseService;

    public ReplayService(TrafficLogService trafficLogService, DatabaseService databaseService) {
        this.trafficLogService = trafficLogService;
        this.databaseService = databaseService;
    }

    public List<ReplayResult> replay(String targetDatabaseId) {
        List<QueryLogEntry> entries = trafficLogService.getAll();
        log.info("Replaying {} logged queries against sandbox {}", entries.size(), targetDatabaseId);

        return entries.stream()
                .map(entry -> {
                    Map<String, Object> result = databaseService.execute(targetDatabaseId, entry.sql());
                    boolean success = Boolean.TRUE.equals(result.get("success"));
                    String errorMessage = success ? null : firstNonNull(result.get("error"), result.get("message"));
                    return new ReplayResult(entry.sql(), success, errorMessage);
                })
                .collect(Collectors.toList());
    }

    private String firstNonNull(Object a, Object b) {
        if (a != null) return String.valueOf(a);
        if (b != null) return String.valueOf(b);
        return "Unknown error";
    }
}