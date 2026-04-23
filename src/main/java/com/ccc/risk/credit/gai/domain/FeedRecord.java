package com.ccc.risk.credit.gai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedRecord {
    private String category; // EVENT, RECORD, or ATTRIBUTE
    private Map<String, String> values = new HashMap<>();

    public FeedRecord(String category) {
        this.category = category;
        this.values = new HashMap<>();
    }

    public void setValue(String key, String value) {
        values.put(key, value != null ? value : "");
    }

    public String getValue(String key) {
        return values.getOrDefault(key, "");
    }
}
