package com.shadowbase.service;

import com.shadowbase.dto.DatabaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

    private final Map<String, PostgreSQLContainer<?>> containers = new ConcurrentHashMap<>();

    public DatabaseInfo spinUp() {
        log.info("Spinning up a new PostgreSQL container...");

        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("ShadowBase")
                .withUsername("shadow")
                .withPassword("shadow");

        container.start();

        String id = UUID.randomUUID().toString();
        containers.put(id, container);

        log.info("Container {} is up on port {}", id, container.getFirstMappedPort());

        return new DatabaseInfo(
                id,
                container.getHost(),
                container.getFirstMappedPort(),
                container.getDatabaseName(),
                container.getUsername(),
                container.getPassword(),
                container.getJdbcUrl()
        );
    }

    public int runningCount() {
        return containers.size();
    }
}