package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executes SQL queries against the SCEF Oracle datasource.
 *
 * <p>SQL files are loaded from {@code classpath:sql/{feedName}_{category}_query.sql}.
 * No query parameters are used — all records are returned for the configured feed type.
 */
@Slf4j
@Service
public class DatabaseQueryService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueryService(@Qualifier("scefDataSource") DataSource scefDataSource) {
        this.jdbcTemplate = new JdbcTemplate(scefDataSource);
    }

    public List<FeedRecord> executeQuery(FeedDefinition definition, String category, String cobDate) {
        String sql = loadSql(definition.getFeedName(), category);

        log.info("Executing SQL for feed='{}' category='{}'",
                definition.getFeedName(), category);

        // No parameters — SQL has no date filter, returns all records
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        log.info("Retrieved {} rows for {}/{}", rows.size(), definition.getFeedName(), category);

        return rows.stream()
                .map(row -> convertToFeedRecord(row, category))
                .collect(Collectors.toList());
    }

    private FeedRecord convertToFeedRecord(Map<String, Object> row, String category) {
        FeedRecord record = new FeedRecord(category);
        record.setFields(row);
        return record;
    }

    private String loadSql(String feedName, String category) {
        String path = "sql/" + feedName + "_" + category.toLowerCase() + "_query.sql";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "SQL file not found on classpath: " + path
                    + ". Create src/main/resources/" + path, e);
        }
    }
}
