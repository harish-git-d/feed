package com.ccc.risk.credit.gai.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

/**
 * Configures two datasources so that Spring Batch metadata tables never
 * touch Oracle — avoiding the need for any DDL permissions on the SCEF schema.
 *
 * <h3>batchDataSource (H2 in-memory)</h3>
 * Used exclusively by Spring Batch for its metadata tables
 * (BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION, etc.).
 * The schema is auto-created from the script bundled inside
 * {@code spring-batch-core.jar} — no files to manage, no DBA involvement.
 * Data is ephemeral: it exists only for the duration of the JVM process.
 * The {@code @BatchDataSource} annotation tells Spring Boot's Batch
 * auto-configuration to use this datasource instead of the primary one.
 *
 * <h3>scefDataSource (Oracle — primary)</h3>
 * Used for all SCEF application data queries. Your Oracle user only needs
 * {@code SELECT} on the relevant SCEF views/tables — no DDL permissions required.
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    // -------------------------------------------------------------------------
    // Spring Batch metadata — H2 in-memory (no Oracle permissions needed)
    // -------------------------------------------------------------------------

    /**
     * H2 in-memory datasource for Spring Batch metadata tables.
     * Schema is initialised automatically from the script shipped with
     * {@code spring-batch-core} — {@code schema-h2.sql}.
     */
    @Bean
    @BatchDataSource
    public DataSource batchDataSource() {
        log.info("Initialising Spring Batch metadata store (H2 in-memory)");
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("batchDb")
                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                .build();
    }

    @Bean("batchTransactionManager")
    public JdbcTransactionManager batchTransactionManager(
            @Qualifier("batchDataSource") DataSource batchDataSource) {
        return new JdbcTransactionManager(batchDataSource);
    }

    // -------------------------------------------------------------------------
    // SCEF Oracle datasource — primary for all application queries
    // -------------------------------------------------------------------------

    /**
     * Provides the {@link DataSourceProperties} bound from
     * {@code spring.datasource.*} in application YAML.
     * Kept separate so the {@code scefDataSource} bean can be typed as
     * {@link HikariDataSource} for pool-level config access if needed.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties scefDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Oracle HikariCP datasource for SCEF data queries.
     * Only needs SELECT privileges on SCEF views/tables — no DDL.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource scefDataSource(
            @Qualifier("scefDataSourceProperties") DataSourceProperties properties) {
        log.info("Initialising SCEF Oracle datasource: {}", properties.getUrl());
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    @Qualifier("scefTransactionManager")
    public JdbcTransactionManager scefTransactionManager(
            @Qualifier("scefDataSource") DataSource scefDataSource) {
        return new JdbcTransactionManager(scefDataSource);
    }
}
