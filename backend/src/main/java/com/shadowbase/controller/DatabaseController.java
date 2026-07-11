package com.shadowbase.controller;

import com.shadowbase.dto.DatabaseInfo;
import com.shadowbase.service.DatabaseService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/db")
@CrossOrigin(origins = "http://localhost:5173")
public class DatabaseController {

    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping("/spin-up")
    public DatabaseInfo spinUp() {
        return databaseService.spinUp();
    }

    @GetMapping("/count")
    public Map<String, Integer> count() {
        return Map.of("running", databaseService.runningCount());
    }
}