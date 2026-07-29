package com.shadowbase.service;
import com.shadowbase.dto.DatabaseInfo; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.LinkedHashMap;
import java.sql.ResultSetMetaData;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);
    private final TrafficLogService trafficLogService;
    private final Map<String, PostgreSQLContainer> containers = new ConcurrentHashMap<>();

    public DatabaseService(TrafficLogService trafficLogService){
        this.trafficLogService = trafficLogService;
    }

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

    public Map<String, Object> execute(String id, String sql) {
        if (sql == null || sql.isBlank()) {
            return Map.of("success", false, "error", "No SQL provided");
        }

        Map<String, Object> result = runSql(id, sql);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        String errorMessage = success ? null : String.valueOf(result.get("error"));
        trafficLogService.record(id, sql, success, errorMessage);
        return result;
    }

    public Map<String, Object> executeForReplay(String id, String sql) {
        if (sql == null || sql.isBlank()) {
            return Map.of("success", false, "error", "No SQL provided");
        }
        return runSql(id, sql);
    }

    private Map<String, Object> runSql(String id, String sql) {
        PostgreSQLContainer container = require(id);

        try (Connection conn = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
            Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    return Map.of(
                            "success", true,
                            "rows", resultSetToList(rs)
                    );
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                return Map.of(
                        "success", true,
                        "message", "Statement executed. Rows affected: " + updateCount
                );
            }

        } catch (SQLException e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            log.warn("SQL error on database {}: {}", id, message);
            return Map.of("success", false, "error", message);
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
    
    public void destroy(String id) {
        PostgreSQLContainer container = require(id);
        container.stop();          
        containers.remove(id);     
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