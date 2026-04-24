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

/**
 * Executes SQL queries against the SCEF Oracle database and returns results
 * as {@link FeedRecord} instances ready for file writing.
 *
 * <p>SQL files are loaded from {@code classpath:sql/{feedName}_{category}_query.sql},
 * e.g. {@code sql/stress-exposure_event_query.sql}. Each SQL must accept a
 * single positional parameter {@code ?} for the COB date.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseQueryService {

    private final JdbcTemplate jdbcTemplate;

    public List<FeedRecord> executeQuery(FeedDefinition definition, String category, String cobDate) {
        String sql = loadSql(definition.getFeedName(), category);

        log.info("Executing SQL for feed='{}' category='{}' cobDate='{}'",
                definition.getFeedName(), category, cobDate);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, cobDate);

        log.info("Retrieved {} rows for {}/{}", rows.size(), definition.getFeedName(), category);

        return rows.stream()
                .map(row -> convertToFeedRecord(row, category))
                .collect(Collectors.toList());
    }

    private FeedRecord convertToFeedRecord(Map<String, Object> row, String category) {
        FeedRecord record = new FeedRecord(category);
        record.setFields(row);   // uses the fixed FeedRecord.setFields(Map<String,Object>)
        return record;
    }

    private String loadSql(String feedName, String category) {
        // Naming convention: {feedName}_{category}_query.sql  (all lowercase)
        String path = "sql/" + feedName + "_" + category.toLowerCase() + "_query.sql";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "SQL file not found on classpath: " + path +
                    ". Create src/main/resources/" + path, e);
        }
    }
}
