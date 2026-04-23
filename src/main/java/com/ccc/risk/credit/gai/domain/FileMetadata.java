package com.ccc.risk.credit.gai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private String fileName;
    private String fileType; // EVENT, RECORD, ATTRIBUTE, CONTROL
    private long recordCount;
    private long fileSizeBytes;
    private String filePath;
}
