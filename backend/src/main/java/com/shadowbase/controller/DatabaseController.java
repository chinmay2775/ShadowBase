package com.shadowbase.controller;
import com.shadowbase.dto.DatabaseInfo;
import com.shadowbase.service.DatabaseService;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/{id}/seed")
    public Map<String, String> seed(@PathVariable String id) {
        return Map.of("message", databaseService.seed(id));
    }

    @PostMapping("/{id}/execute")
    public Map<String, Object> execute(@PathVariable String id, @RequestBody Map<String, String> body) {
        return databaseService.execute(id, body.get("sql"));
    }
    
    @DeleteMapping("/{id}")
    public Map<String, String> destroy(@PathVariable String id) {
        databaseService.destroy(id);
        return Map.of("message", "Database " + id + " destroyed");
    }

    @GetMapping("/count")
    public Map<String, Integer> count() {
        return Map.of("running", databaseService.runningCount());
    }
}