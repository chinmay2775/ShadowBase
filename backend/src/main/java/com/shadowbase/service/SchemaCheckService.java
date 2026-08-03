package com.shadowbase.service;

import com.shadowbase.dto.SchemaCheckResult;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SchemaCheckService {

    private static final Logger log = LoggerFactory.getLogger(SchemaCheckService.class);

    private final DatabaseService databaseService;

    public SchemaCheckService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public SchemaCheckResult check(String targetDatabaseId, String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tables = tablesNamesFinder.getTableList(statement);

            Set<String> referencedColumns = extractColumns(statement);
            List<String> issues = new ArrayList<>();

            for (String table : tables) {
                Set<String> realColumns = databaseService.getColumnNames(targetDatabaseId, table);
                if (realColumns.isEmpty()) {
                    issues.add("Table '" + table + "' was not found in the target database");
                    continue;
                }
                for (String column : referencedColumns) {
                    if (!realColumns.contains(column.toLowerCase())) {
                        issues.add("Column '" + column + "' does not exist on table '" + table + "'");
                    }
                }
            }

            boolean safe = issues.isEmpty();
            return new SchemaCheckResult(sql, tables, new ArrayList<>(referencedColumns), safe, issues);

        } catch (Exception e) {
            log.warn("Failed to check query against schema: {}", e.getMessage());
            return new SchemaCheckResult(sql, List.of(), List.of(), false, List.of("Could not parse query: " + e.getMessage()));
        }
    }

    /** Collects every column name mentioned in the query's SELECT list (e.g. "SELECT name, email FROM ..."). */
    private Set<String> extractColumns(Statement statement) {
        Set<String> columns = new LinkedHashSet<>();

        if (statement instanceof PlainSelect plainSelect) {
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                if (item.getExpression() instanceof Column column) {
                    columns.add(column.getColumnName());
                }
            }
        }

        return columns;
    }
}