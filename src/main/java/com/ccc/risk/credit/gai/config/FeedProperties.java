package com.ccc.risk.credit.gai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gai")
public class FeedProperties {

    @NotBlank
    private String sourceSystemCsi;
    @NotBlank
    private String sourceSystemName;
    @NotBlank
    private String appShortName;
    @NotBlank
    private String regionInstance;
    @NotBlank
    private String sourceIndicator;
    @NotBlank
    private String frequency;
    @NotBlank
    private String fileTypeIndicator;
    @NotBlank
    private String outputDirectory;
    @NotBlank
    private String feedName;
    @NotBlank
    private String cobDate;
    @NotBlank
    private String fileTimestamp;
    private boolean gzipEnabled = true;
    private final Sftp sftp = new Sftp();

    public static class Sftp {
        private boolean enabled;
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String remoteDirectory;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRemoteDirectory() { return remoteDirectory; }
        public void setRemoteDirectory(String remoteDirectory) { this.remoteDirectory = remoteDirectory; }
    }
}
