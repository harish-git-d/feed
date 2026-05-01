package com.ccc.risk.credit.gai.domain;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Defines the structure of a single GAI feed, loaded from a YAML file
 * under {@code classpath:feed-definitions/}.
 *
 * <p>Three separate field lists correspond to the three GAI ADIL file types:
 * <ul>
 *   <li>{@code eventFields}     — columns for the EVENT file (E1-E24)</li>
 *   <li>{@code recordFields}    — columns for the RECORD file (R1-R28)</li>
 *   <li>{@code attributeFields} — columns for the ATTRIBUTE file (A1-A33)</li>
 * </ul>
 *
 * <p>{@code dateColumns} is a map of column alias → date format pattern.
 * Each date column can have its own independent format. Falls back to
 * the global {@code gai.date-format} if the column's value is blank/null.
 */
@Data
public class FeedDefinition {

    private String feedName;
    private String moduleName;
    private String sourceSystemCsi;
    private String sourceSystemName;
    private String delimiter = "|";

    /** EVENT file column names in output order (E1–E24) */
    private List<String> eventFields;

    /** RECORD file column names in output order (R1–R28) */
    private List<String> recordFields;

    /** ATTRIBUTE file column names in output order (A1–A33) */
    private List<String> attributeFields;

    /**
     * Per-column date format patterns.
     *
     * <p>Key   = column alias (must match the SQL SELECT alias exactly).
     * <p>Value = Java {@link java.text.SimpleDateFormat} pattern to apply.
     *
     * <p>If a column is listed with a blank/null value, the global
     * {@code gai.date-format} from application YAML is used as fallback.
     * If a column is not listed at all, its value is written as-is from the DB.
     *
     * <p>Example in feed YAML:
     * <pre>
     * dateColumns:
     *   EFFECTIVE_DATE:         MMddyyyy
     *   POSTED_DATE:            MMddyyyy
     *   COB_DATE:               MMddyyyy
     *   EVENT_DATE_AND_TIME:    MM/dd/yyyy HH:mm:ss
     *   RESOLUTION_TARGET_DATE: yyyy-MM-dd
     * </pre>
     */
    private Map<String, String> dateColumns;
}
