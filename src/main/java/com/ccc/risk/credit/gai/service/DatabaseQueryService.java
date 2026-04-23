package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseQueryService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Executes SQL query and returns results as FeedRecords
     */
    public List<FeedRecord> executeQuery(FeedDefinition definition, String category, String cobDate) {
        String sql = loadSql(definition.getFeedName(), category);

        log.info("Executing SQL for feed: {}, category: {}", definition.getFeedName(), category);
        log.debug("SQL: {}", sql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, cobDate);

        log.info("Retrieved {} rows from database", rows.size());

        return rows.stream()
                .map(row -> convertToFeedRecord(row, definition))
                .collect(Collectors.toList());
    }

    private String loadSql(String feedName, String category) {
        String path = "sql/" + feedName + "_" + category.toLowerCase() + ".sql";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("SQL file not found: " + path, e);
        }
    }

    private FeedRecord convertToFeedRecord(Map<String, Object> row, FeedDefinition definition) {
        FeedRecord record = new FeedRecord();
        record.setFields(row);
        return record;
    }
}
