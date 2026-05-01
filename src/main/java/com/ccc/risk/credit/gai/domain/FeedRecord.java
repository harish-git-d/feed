package com.ccc.risk.credit.gai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents one row of data to be written to a GAI feed file.
 * Column order is preserved via LinkedHashMap.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedRecord {

    private String category; // EVENT, RECORD, or ATTRIBUTE

    private Map<String, String> values = new LinkedHashMap<>();

    public FeedRecord(String category) {
        this.category = category;
        this.values   = new LinkedHashMap<>();
    }

    public void setValue(String key, String value) {
        values.put(key, value != null ? value : "");
    }

    public String getValue(String key) {
        return values.getOrDefault(key, "");
    }

    /**
     * Populates values from a raw JDBC result row.
     * Converts all values to String; nulls become empty string.
     */
    public void setFields(Map<String, Object> row) {
        row.forEach((k, v) -> values.put(k, v != null ? v.toString() : ""));
    }
}
