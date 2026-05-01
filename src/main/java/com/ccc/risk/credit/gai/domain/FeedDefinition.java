package com.ccc.risk.credit.gai.domain;

import lombok.Data;
import java.util.List;
import java.util.Set;

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
 * <p>{@code dateColumns} lists the column aliases that contain date/timestamp
 * values. These are formatted using {@code gai.date-format} from application
 * YAML at write time, rather than being formatted in SQL.
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
     * Column aliases that hold date/timestamp values and should be
     * formatted using {@code gai.date-format}.
     *
     * <p>SQL queries return these as raw {@link java.sql.Timestamp} or
     * {@link java.util.Date} objects. The writer applies the configured
     * format pattern at output time.
     *
     * <p>Example in feed YAML:
     * <pre>
     * dateColumns:
     *   - EFFECTIVE_DATE
     *   - POSTED_DATE
     *   - COB_DATE
     *   - EVENT_DATE_AND_TIME
     * </pre>
     */
    private Set<String> dateColumns;
}
