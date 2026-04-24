package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import org.springframework.stereotype.Service;

/**
 * Builds GAI 2.0 standard feed file names.
 *
 * <p>Pattern:
 * <pre>
 *   {CSI}_{BU}_{SourceSystem}-{ModuleName}_{Region}_{FileType}_{Freq}_{FeedType}_{Source}_{COBDate}_{Timestamp}.dat[.gz]
 * </pre>
 *
 * <p>Example:
 * <pre>
 *   161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz
 * </pre>
 *
 * <p>The business unit ({@code CRC}) is extracted from {@code gai.app-short-name}
 * which is configured as {@code CRC_SCEF}. The source system ({@code SCEF}) comes
 * from {@code gai.source-system-name}.
 */
@Service
public class FeedFileNamingService {

    private final FeedProperties properties;

    public FeedFileNamingService(FeedProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds the file name for a data file (EVENT, RECORD, or ATTRIBUTE).
     *
     * @param definition the feed definition (provides moduleName)
     * @param fileType   one of EVENT, RECORD, ATTRIBUTE
     * @return e.g. {@code 161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz}
     */
    public String buildDataFileName(FeedDefinition definition, String fileType) {
        return String.format("%s_%s_%s-%s_%s_%s_%s_%s_%s_%s_%s.dat%s",
                properties.getSourceSystemCsi(),    // 161534
                extractBusinessUnit(),               // CRC
                properties.getSourceSystemName(),   // SCEF
                definition.getModuleName(),          // STRESSEXP
                properties.getRegionInstance(),      // N
                fileType,                            // EVENT / RECORD / ATTRIBUTE
                properties.getFrequency(),           // DLY
                properties.getFileTypeIndicator(),   // F
                properties.getSourceIndicator(),     // SRC
                normaliseCobDate(),                  // 20251231
                properties.getFileTimestamp(),       // 20260122144002
                properties.isGzipEnabled() ? ".gz" : ""
        );
    }

    /**
     * Builds the file name for a CONTROL file.
     *
     * @param definition the feed definition
     * @return e.g. {@code 161534_CRC_SCEF-STRESSEXP_N_CONTROL_DLY_F_SRC_20251231_20260122144002.dat.gz}
     */
    public String buildControlFileName(FeedDefinition definition) {
        return buildDataFileName(definition, "CONTROL");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Extracts the business unit prefix from {@code gai.app-short-name}.
     * {@code "CRC_SCEF"} → {@code "CRC"}.
     */
    private String extractBusinessUnit() {
        String appShortName = properties.getAppShortName();
        return appShortName.contains("_") ? appShortName.split("_")[0] : appShortName;
    }

    /** Strips hyphens to normalise {@code 2025-12-31} → {@code 20251231}. */
    private String normaliseCobDate() {
        return properties.getCobDate().replace("-", "");
    }
}
