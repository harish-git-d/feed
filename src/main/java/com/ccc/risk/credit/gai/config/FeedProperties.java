package com.ccc.risk.credit.gai.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Centralised configuration properties for GAI feed generation.
 * Bound from {@code gai.*} in application YAML.
 *
 * <p>Minimum required at startup:
 * <ul>
 *   <li>{@code gai.feed-name}      — e.g. {@code stress-exposure}</li>
 *   <li>{@code gai.cob-date}       — e.g. {@code 2025-12-31} or {@code 20251231}</li>
 *   <li>{@code gai.file-timestamp} — e.g. {@code 20260122144002}</li>
 * </ul>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "gai")
public class FeedProperties {

    @NotBlank private String sourceSystemCsi  = "161534";
    @NotBlank private String sourceSystemName = "SCEF";
    @NotBlank private String appShortName     = "CRC_SCEF";
    @NotBlank private String regionInstance   = "N";
    @NotBlank private String sourceIndicator  = "SRC";
    @NotBlank private String frequency        = "DLY";
    @NotBlank private String fileTypeIndicator = "F";
    @NotBlank private String outputDirectory  = "./output";
    @NotBlank private String feedName;
    @NotBlank private String cobDate;
    @NotBlank private String fileTimestamp;
    private boolean gzipEnabled = true;

    private final Sftp sftp = new Sftp();
    private final Notification notification = new Notification();

    @Getter
    @Setter
    public static class Sftp {
        private boolean enabled       = false;
        private String  host;
        private int     port          = 22;
        private String  username;
        private String  password;
        private String  privateKeyPath;
        private String  knownHostsPath;
        private String  remoteDirectory;
    }

    @Getter
    @Setter
    public static class Notification {
        private boolean enabled  = false;
        private String  from;
        private String  to;
        private String  smtpHost = "localhost";
        private int     smtpPort = 25;
    }
}
