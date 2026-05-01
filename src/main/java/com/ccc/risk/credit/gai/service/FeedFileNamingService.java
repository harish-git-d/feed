package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import org.springframework.stereotype.Service;

/**
 * Builds GAI 2.0 standard feed file names.
 *
 * <p>Data files (EVENT / RECORD / ATTRIBUTE):
 * <pre>
 *   {CSI}_{BU}_{SourceSystem}-{ModuleName}_{Region}_{FileType}_{Freq}_{FeedType}_{Source}_{COBDate}_{Timestamp}.dat[.gz]
 * </pre>
 *
 * <p>Control file:
 * <pre>
 *   {CSI}_{BU}_{SourceSystem}-{ModuleName}_{Region}_CONTROL_{Freq}_{FeedType}_{Source}_{COBDate}_{Timestamp}.ctrl
 * </pre>
 *
 * <p>Example data file:
 * <pre>
 *   161534_CRC_SCEF-STRESSEXP_N_EVENT_DLY_F_SRC_20251231_20260122144002.dat.gz
 * </pre>
 *
 * <p>Example control file:
 * <pre>
 *   161534_CRC_SCEF-STRESSEXP_N_CONTROL_DLY_F_SRC_20251231_20260122144002.ctrl
 * </pre>
 */
@Service
public class FeedFileNamingService {

    private final FeedProperties properties;

    public FeedFileNamingService(FeedProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds the file name for a data file (EVENT, RECORD, or ATTRIBUTE).
     * Extension: .dat or .dat.gz depending on gzip setting.
     */
    public String buildDataFileName(FeedDefinition definition, String fileType) {
        return String.format("%s_%s_%s-%s_%s_%s_%s_%s_%s_%s_%s.dat%s",
                properties.getSourceSystemCsi(),
                extractBusinessUnit(),
                properties.getSourceSystemName(),
                definition.getModuleName(),
                properties.getRegionInstance(),
                fileType,
                properties.getFrequency(),
                properties.getFileTypeIndicator(),
                properties.getSourceIndicator(),
                normaliseCobDate(),
                properties.getFileTimestamp(),
                properties.isGzipEnabled() ? ".gz" : ""
        );
    }

    /**
     * Builds the file name for the control/trigger file.
     * Extension: always .ctrl (not gzipped, not .dat).
     */
    public String buildControlFileName(FeedDefinition definition) {
        return String.format("%s_%s_%s-%s_%s_CONTROL_%s_%s_%s_%s_%s.ctrl",
                properties.getSourceSystemCsi(),
                extractBusinessUnit(),
                properties.getSourceSystemName(),
                definition.getModuleName(),
                properties.getRegionInstance(),
                properties.getFrequency(),
                properties.getFileTypeIndicator(),
                properties.getSourceIndicator(),
                normaliseCobDate(),
                properties.getFileTimestamp()
        );
    }

    private String extractBusinessUnit() {
        String appShortName = properties.getAppShortName();
        return appShortName.contains("_") ? appShortName.split("_")[0] : appShortName;
    }

    private String normaliseCobDate() {
        return properties.getCobDate().replace("-", "");
    }
}
