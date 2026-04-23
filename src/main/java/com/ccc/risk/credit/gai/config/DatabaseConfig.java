package com.ccc.risk.credit.gai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private static final String DOMAIN_PACKAGES_TO_SCAN = "com.ccc.risk.credit.gai.domain";

    @Bean(name = "hikariConfig")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("ApplicationHikariPool");
        return config;
    }

    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource() {
        HikariConfig config = hikariConfig();
        // Set required properties from application.yml
        config.setJdbcUrl("${spring.datasource.url}");
        config.setUsername("${spring.datasource.username}");
        config.setPassword("${spring.datasource.password}");
        config.setDriverClassName("${spring.datasource.driver-class-name}");
        return new HikariDataSource(config);
    }

    @Primary
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    @ConfigurationProperties(prefix = "spring.jpa.properties")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPersistenceUnitName("default");
        em.setDataSource(dataSource);
        em.setPackagesToScan(DOMAIN_PACKAGES_TO_SCAN);
        em.setJpaVendorAdapter(createVendorAdapter());
        // Hibernate properties will be bound from spring.jpa.properties.*
        return em;
    }

    private HibernateJpaVendorAdapter createVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.Oracle12cDialect");
        return adapter;
    }
}