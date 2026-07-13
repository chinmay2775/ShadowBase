package com.shadowbase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.shadowbase.dto.DatabaseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.web.server.ResponseStatusException;


import java.util.Map;
import java.util.UUID;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

    private final Map<String, PostgreSQLContainer> containers = new ConcurrentHashMap<>();

    public DatabaseInfo spinUp() {
        log.info("Spinning up a new PostgreSQL container...");

        PostgreSQLContainer container = new PostgreSQLContainer(
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

    public String seed(String id) {
        PostgreSQLContainer container = require(id);

        // Open a real JDBC connection to the running container, using its own details.
        try (Connection conn = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id    SERIAL PRIMARY KEY,
                    name  VARCHAR(100) NOT NULL,
                    email VARCHAR(200) NOT NULL
                )
                """);

            int rows = stmt.executeUpdate("""
                INSERT INTO customers (name, email) VALUES
                    ('Chinmay Chaudhari', 'chinmay@gmail.com'),
                    ('Lalit Patil',  'lalit@gmail.com'),
                    ('Rohit Salvi', 'rohit@gmail.com')
                """);

            log.info("Seeded database {} with {} rows", id, rows);
            return "Created 'customers' table and inserted " + rows + " rows";

        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed database " + id + ": " + e.getMessage(), e);
        }
    }

    public void destroy(String id) {
        PostgreSQLContainer container = require(id);
        container.stop();          // stops and removes the Docker container
        containers.remove(id);     // forget it on our side too
        log.info("Destroyed database {}", id);
    }

    private PostgreSQLContainer require(String id) {
        PostgreSQLContainer container = containers.get(id);
        if (container == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No database with id " + id);
        }
        return container;
    }
}