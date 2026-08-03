package com.shadowbase.controller;

import com.shadowbase.dto.SchemaCheckResult;
import com.shadowbase.service.SchemaCheckService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class SchemaCheckController {

    private final SchemaCheckService schemaCheckService;

    public SchemaCheckController(SchemaCheckService schemaCheckService) {
        this.schemaCheckService = schemaCheckService;
    }

    @PostMapping("/schema-check/{targetDatabaseId}")
    public SchemaCheckResult check(@PathVariable String targetDatabaseId, @RequestBody Map<String, String> body) {
        return schemaCheckService.check(targetDatabaseId, body.get("sql"));
    }
}