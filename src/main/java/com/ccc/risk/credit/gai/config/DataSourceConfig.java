package com.ccc.risk.credit.gai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

/**
 * Manually configures two datasources.
 *
 * <p>Spring Boot's {@code DataSourceAutoConfiguration} is excluded in
 * {@link com.ccc.risk.credit.gai.FeedBatchApplication} to prevent Boot
 * from creating a third conflicting datasource bean from
 * {@code spring.datasource.*} properties.
 *
 * <ul>
 *   <li><b>batchDataSource</b> — H2 in-memory, tagged {@code @BatchDataSource}.
 *       Spring Batch metadata tables auto-created from {@code schema-h2.sql}
 *       bundled in {@code spring-batch-core.jar}. No Oracle permissions needed.</li>
 *   <li><b>scefDataSource</b> — Oracle HikariCP, {@code @Primary}.
 *       Used for all SCEF data queries. Needs only SELECT on SCEF views/tables.</li>
 * </ul>
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    // -------------------------------------------------------------------------
    // H2 — Spring Batch metadata (no Oracle permissions needed)
    // -------------------------------------------------------------------------

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
    // Oracle — SCEF application data (SELECT only)
    // -------------------------------------------------------------------------

    @Bean
    @Primary
    public DataSource scefDataSource(Environment env) {
        String url      = env.getRequiredProperty("spring.datasource.url");
        String username = env.getRequiredProperty("spring.datasource.username");
        String password = env.getRequiredProperty("spring.datasource.password");

        log.info("Initialising SCEF Oracle datasource: {}", url);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("ScefOraclePool");
        config.setMinimumIdle(
                env.getProperty("spring.datasource.hikari.minimum-idle", Integer.class, 2));
        config.setMaximumPoolSize(
                env.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class, 10));
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");

        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    @Qualifier("scefTransactionManager")
    public JdbcTransactionManager scefTransactionManager(
            @Qualifier("scefDataSource") DataSource scefDataSource) {
        return new JdbcTransactionManager(scefDataSource);
    }
}
