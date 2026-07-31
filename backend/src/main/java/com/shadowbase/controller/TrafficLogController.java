package com.shadowbase.controller;

import com.shadowbase.dto.QueryLogEntry;
import com.shadowbase.service.TrafficLogService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TrafficLogController {

    private final TrafficLogService trafficLogService;

    public TrafficLogController(TrafficLogService trafficLogService) {
        this.trafficLogService = trafficLogService;
    }

    @GetMapping("/traffic-log")
    public List<QueryLogEntry> trafficLog() {
        return trafficLogService.getAll();
    }

    @DeleteMapping("/traffic-log")
    public Map<String,String> clearTrafficLog(){
        trafficLogService.clear();
        return Map.of("Message","Traffic Log Cleared");
    }
}