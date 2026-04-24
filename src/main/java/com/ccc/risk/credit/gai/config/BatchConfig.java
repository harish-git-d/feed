package com.ccc.risk.credit.gai.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration.
 *
 * <p>Spring Boot 3 + Spring Batch 5 auto-configures the JobRepository,
 * JobLauncher, and TransactionManager from the primary DataSource.
 * No manual wiring is needed — @EnableBatchProcessing is intentionally
 * omitted because it disables Boot's auto-configuration in SBatch 5.
 *
 * <p>Batch metadata tables are initialized from the H2 schema on test
 * profile and must exist in Oracle for DEV/SIT/UAT/PROD
 * (run spring-batch-core schema-oracle.sql once per environment).
 */
@Configuration
public class BatchConfig {
    // Auto-configured by Spring Boot:
    //   - JobRepository    (backed by primary DataSource)
    //   - JobLauncher      (async or sync via spring.batch.job.*)
    //   - TransactionManager (HikariCP -> DataSourceTransactionManager)
}
