package com.ccc.risk.credit.gai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

/**
 * Configures the single Oracle datasource for SCEF data queries.
 *
 * <p>Spring Batch metadata no longer uses any datasource — it is stored
 * entirely in-memory via {@link InMemoryJobRepository} in {@link BatchConfig}.
 * H2 has been removed from the project entirely.
 *
 * <p>Only SELECT privileges on SCEF views/tables are required.
 * No DDL permissions needed anywhere.
 */
@Slf4j
@Configuration
public class DataSourceConfig {

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
    public JdbcTransactionManager scefTransactionManager(DataSource scefDataSource) {
        return new JdbcTransactionManager(scefDataSource);
    }
}
