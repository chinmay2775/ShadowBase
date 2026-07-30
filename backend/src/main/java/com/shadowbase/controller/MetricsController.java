package com.shadowbase.controller;

import com.shadowbase.service.MetricsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of("eventsCaptured", metricsService.getEventsCaptured(),
                "queriesReplayed", metricsService.getQueriesReplayed(), 
                "errorRate", metricsService.getErrorRatePercent()
        );
    }
}