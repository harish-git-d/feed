package com.ccc.risk.credit.gai.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Centralised configuration properties for GAI feed generation.
 * Bound from {@code gai.*} in application YAML.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "gai")
public class FeedProperties {

    @NotBlank private String sourceSystemCsi   = "161534";
    @NotBlank private String sourceSystemName  = "SCEF";
    @NotBlank private String appShortName      = "CRC_SCEF";
    @NotBlank private String regionInstance    = "N";
    @NotBlank private String sourceIndicator   = "SRC";
    @NotBlank private String frequency         = "DLY";
    @NotBlank private String fileTypeIndicator = "F";
    @NotBlank private String outputDirectory   = "./output";
    @NotBlank private String feedName;
    @NotBlank private String cobDate;
    @NotBlank private String fileTimestamp;
    private boolean gzipEnabled = true;

    /**
     * Java date format pattern applied to all date columns in output files.
     *
     * <p>Common values:
     * <ul>
     *   <li>{@code MMddyyyy} — e.g. 06122024  (GAI default)</li>
     *   <li>{@code yyyyMMdd} — e.g. 20240612  (ISO compact)</li>
     *   <li>{@code MM/dd/yyyy} — e.g. 06/12/2024</li>
     *   <li>{@code yyyy-MM-dd} — e.g. 2024-06-12 (ISO)</li>
     *   <li>{@code MM/dd/yyyy HH:mm:ss} — with time</li>
     * </ul>
     *
     * <p>Configure in application YAML: {@code gai.date-format: MMddyyyy}
     */
    @NotBlank private String dateFormat = "MMddyyyy";

    private final Sftp         sftp         = new Sftp();
    private final Notification notification = new Notification();

    @Getter @Setter
    public static class Sftp {
        private boolean enabled        = false;
        private String  host;
        private int     port           = 22;
        private String  username;
        private String  password;
        private String  privateKeyPath;
        private String  knownHostsPath;
        private String  remoteDirectory;
    }

    @Getter @Setter
    public static class Notification {
        private boolean enabled  = false;
        private String  from;
        private String  to;
        private String  smtpHost = "localhost";
        private int     smtpPort = 25;
    }
}
