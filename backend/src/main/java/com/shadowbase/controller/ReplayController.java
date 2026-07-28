package com.shadowbase.controller;

import com.shadowbase.dto.ReplayResult;
import com.shadowbase.service.ReplayService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    @PostMapping("/replay/{targetDatabaseId}")
    public List<ReplayResult> replay(@PathVariable String targetDatabaseId) {
        return replayService.replay(targetDatabaseId);
    }
}