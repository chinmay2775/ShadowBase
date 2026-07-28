package com.shadowbase.service;

import com.shadowbase.dto.QueryLogEntry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class TrafficLogService {

    private final List<QueryLogEntry> entries = new CopyOnWriteArrayList<>();

    public void record(String databaseId, String sql, boolean success, String errorMessage) {
        entries.add(new QueryLogEntry(databaseId, sql, Instant.now(), success, errorMessage));
    }

    public List<QueryLogEntry> getAll() {
        return List.copyOf(entries);
    }
}