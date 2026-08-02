package com.shadowbase.controller;

import com.shadowbase.dto.QueryAnalysis;
import com.shadowbase.service.QueryAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class QueryAnalysisController {

    private final QueryAnalysisService queryAnalysisService;

    public QueryAnalysisController(QueryAnalysisService queryAnalysisService) {
        this.queryAnalysisService = queryAnalysisService;
    }

    @PostMapping("/analyze")
    public QueryAnalysis analyze(@RequestBody Map<String, String> body) {
        return queryAnalysisService.analyze(body.get("sql"));
    }
}