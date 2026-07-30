package com.shadowbase.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;


@Service
public class MetricsService {

    private final AtomicLong eventsCaptured = new AtomicLong(0);
    private final AtomicLong queriesReplayed = new AtomicLong(0);
    private final AtomicLong queriesFailed = new AtomicLong(0);

    public void incrementEventsCaptured() {
        eventsCaptured.incrementAndGet();
    }

    public long getEventsCaptured() {
        return eventsCaptured.get();
    }

    public void recordReplayBatch(long totalReplayed, long failedCount) {
        queriesReplayed.set(totalReplayed);
        queriesFailed.set(failedCount);
    }

    public long getQueriesReplayed() {
        return queriesReplayed.get();
    }

    public int getErrorRatePercent() {
        long replayed = queriesReplayed.get();
        if (replayed == 0) {
            return 0;
        }
        return (int) Math.round((queriesFailed.get() * 100.0) / replayed);
    }
}