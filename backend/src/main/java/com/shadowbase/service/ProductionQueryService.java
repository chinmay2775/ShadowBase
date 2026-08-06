package com.shadowbase.service;

import com.shadowbase.dto.ProductionQueryResult;
import org.springframework.stereotype.Service;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionQueryService {

    private final ProductionEnviormentService productionEnvironmentService;

    public ProductionQueryService(ProductionEnviormentService productionEnvironmentService) {
        this.productionEnvironmentService = productionEnvironmentService;
    }

    public ProductionQueryResult runQuery(String sql) {
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            return new ProductionQueryResult(false, List.of(), List.of(),
                    "Only SELECT queries are allowed against production from this console.");
        }

        PostgreSQLContainer productionDb = productionEnvironmentService.getProductionDb();

        try (Connection conn = DriverManager.getConnection(
                productionDb.getJdbcUrl(), productionDb.getUsername(), productionDb.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(meta.getColumnName(i));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(columns.get(i - 1), rs.getObject(i));
                }
                rows.add(row);
            }

            return new ProductionQueryResult(true, columns, rows, null);
        } catch (Exception e) {
            return new ProductionQueryResult(false, List.of(), List.of(), e.getMessage());
        }
    }
}