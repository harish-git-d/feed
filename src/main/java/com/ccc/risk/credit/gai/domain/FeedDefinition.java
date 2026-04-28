package com.ccc.risk.credit.gai.domain;

import lombok.Data;
import java.util.List;

/**
 * Defines the structure of a single GAI feed, loaded from a YAML file
 * under {@code classpath:feed-definitions/}.
 *
 * <p>Three separate field lists correspond to the three GAI ADIL file types:
 * <ul>
 *   <li>{@code eventFields}     — columns for the EVENT file (E1-E23)</li>
 *   <li>{@code recordFields}    — columns for the RECORD file (R1-R28)</li>
 *   <li>{@code attributeFields} — columns for the ATTRIBUTE file (A1-A32)</li>
 * </ul>
 */
@Data
public class FeedDefinition {

    private String feedName;
    private String moduleName;
    private String sourceSystemCsi;
    private String sourceSystemName;
    private String delimiter = "|";

    /** EVENT file column names in output order (E1, E2, E3...) */
    private List<String> eventFields;

    /** RECORD file column names in output order (R1, R2, R3...) */
    private List<String> recordFields;

    /** ATTRIBUTE file column names in output order (A1, A2, A3...) */
    private List<String> attributeFields;
}
