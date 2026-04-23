package com.ccc.risk.credit.gai.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    @NotBlank
    private String url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String driverClassName;
    private HikariProperties hikari = new HikariProperties();

    @Data
    public static class HikariProperties {
        private int minimumIdle = 5;
        private int maximumPoolSize = 20;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private boolean autoCommit = true;
        private String connectionTestQuery = "SELECT 1 FROM DUAL";
        private int leakDetectionThreshold = 0;
    }
}