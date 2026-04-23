package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import org.springframework.stereotype.Service;

@Service
public class FeedFileNamingService {

    private final FeedProperties properties;

    public FeedFileNamingService(FeedProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds file name for EVENT, RECORD, or ATTRIBUTE files
     * Pattern: {CSI}_{BU}_{SourceSystem}-{ModuleName}_N_{FileType}_DLY_F_SRC_{COBDate}_{Timestamp}.dat.gz
     * Example: 161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz
     */
    public String buildDataFileName(FeedDefinition definition, String fileType) {
        return String.format("%s_%s_%s-%s_%s_%s_%s_%s_%s_%s_%s.dat%s",
                properties.getSourceSystemCsi(),           // 161534
                extractBusinessUnit(),                      // CRC
                properties.getSourceSystemName(),          // SCEF
                definition.getModuleName(),                // STRESSEXP
                properties.getRegionInstance(),            // N
                fileType,                                  // EVENT, RECORD, or ATTRIBUTE
                properties.getFrequency(),                 // DLY
                properties.getFileTypeIndicator(),         // F
                properties.getSourceIndicator(),           // SRC
                formatCobDate(),                           // 20251231
                properties.getFileTimestamp(),             // 20260122144002
                properties.isGzipEnabled() ? ".gz" : ""
        );
    }

    /**
     * Builds control file name
     * Pattern: {CSI}_{BU}_{SourceSystem}-{ModuleName}_N_CONTROL_DLY_F_SRC_{COBDate}_{Timestamp}.dat.gz
     * Example: 161534_CRC_SCEF-STRESSEXP_N_CONTROL_DLY_F_SRC_20251231_20260122144002.dat.gz
     */
    public String buildControlFileName(FeedDefinition definition) {
        return buildDataFileName(definition, "CONTROL");
    }

    private String formatCobDate() {
        // Convert YYYY-MM-DD to YYYYMMDD
        return properties.getCobDate().replace("-", "");
    }

    private String extractBusinessUnit() {
        // Extract "CRC" from "CRC_SCEF"
        String appShortName = properties.getAppShortName();
        return appShortName.contains("_") ? appShortName.split("_")[0] : appShortName;
    }
}
