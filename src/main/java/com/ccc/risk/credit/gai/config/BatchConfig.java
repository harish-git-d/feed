package com.ccc.risk.credit.gai.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration.
 *
 * <p>Spring Boot 3 + Spring Batch 5 auto-configures the JobRepository and
 * JobLauncher automatically. {@code @EnableBatchProcessing} is intentionally
 * omitted — adding it in SBatch 5 disables Boot's auto-configuration.
 *
 * <p><b>No Oracle DDL permissions required.</b>
 * Spring Batch metadata tables are stored in an H2 in-memory database
 * configured by {@link DataSourceConfig}. The Oracle datasource
 * ({@code scefDataSource}) is used only for SCEF data queries and requires
 * only {@code SELECT} privileges on the relevant views/tables.
 *
 * <p>The {@code @BatchDataSource} annotation on the H2 bean in
 * {@link DataSourceConfig} tells Spring Boot's Batch auto-configuration
 * which datasource to use for its metadata schema.
 */
@Configuration
public class BatchConfig {
    // All wiring handled by Spring Boot auto-configuration + DataSourceConfig
}
