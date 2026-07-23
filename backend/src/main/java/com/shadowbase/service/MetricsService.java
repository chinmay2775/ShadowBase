package com.shadowbase.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;


@Service
public class MetricsService {

    private final AtomicLong eventsCaptured = new AtomicLong(0);

    public void incrementEventsCaptured() {
        eventsCaptured.incrementAndGet();
    }

    public long getEventsCaptured() {
        return eventsCaptured.get();
    }
}