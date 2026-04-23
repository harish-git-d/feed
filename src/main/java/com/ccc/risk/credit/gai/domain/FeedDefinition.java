package com.ccc.risk.credit.gai.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FeedDefinition {
    private String feedName;
    private String moduleName;
    private String sourceSystemCsi;
    private String sourceSystemName;
    private String recordType;
    private String attributeName;
    private String adjustmentType;
    private String attributeDescription;
    private String delimiter = "|";
    private String headerTemplate;
    private String trailerTemplate;
    private List<String> recordFields = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Data
    public static class Category {
        private String name;
        private String description;
    }
}
