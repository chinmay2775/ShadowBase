package com.shadowbase.controller;

import com.shadowbase.service.ProductionEnviormentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/production")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductionController {

    private final ProductionEnviormentService productionEnviormentService;

    public ProductionController(ProductionEnviormentService productionEnviormentService) {
        this.productionEnviormentService = productionEnviormentService;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        var db = productionEnviormentService.getProductionDb();
        var kafka = productionEnviormentService.getKafka();
        
        return Map.of(
                "database", Map.of(
                        "host", db.getHost(),
                        "port", db.getFirstMappedPort(),
                        "database", db.getDatabaseName(),
                        "username", db.getUsername(),
                        "password", db.getPassword(),
                        "jdbcUrl", db.getJdbcUrl()
                ),
                "kafka", Map.of(
                        "bootstrapServers", kafka.getBootstrapServers()
                )
        );
    }
}
