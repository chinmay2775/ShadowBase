package com.shadowbase.service;

import com.shadowbase.dto.QueryAnalysis;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(QueryAnalysisService.class);

    public QueryAnalysis analyze(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tables = tablesNamesFinder.getTableList(statement);

            log.info("Parsed query — tables referenced: {}", tables);
            return new QueryAnalysis(sql, tables, true, null);

        } catch (Exception e) {
            log.warn("Failed to parse SQL: {}", e.getMessage());
            return new QueryAnalysis(sql, List.of(), false, e.getMessage());
        }
    }
}