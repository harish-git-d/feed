package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SampleDataFactory {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMddyyyy");

    /**
     * Generates sample records for testing
     * In production, replace this with actual database queries
     */
    public List<FeedRecord> generateRecords(FeedDefinition definition, String category) {
        List<FeedRecord> records = new ArrayList<>();

        // Generate 10 sample records for testing
        for (int i = 1; i <= 10; i++) {
            FeedRecord record = new FeedRecord(category);

            // Common fields
            record.setValue("H", "D"); // Detail record
            record.setValue("ADJ_INVENTORY_ID", "ADJ" + String.format("%06d", i));
            record.setValue("ADJUSTMENT_UPLOAD_ID", "UPL" + String.format("%06d", i));
            record.setValue("ADJUSTMENT_TYPE", definition.getAdjustmentType());
            record.setValue("EFFECTIVE_DATE", LocalDate.now().format(DATE_FORMAT));
            record.setValue("POSTED_DATE", LocalDate.now().format(DATE_FORMAT));
            record.setValue("ATTRIBUTE_NAME", definition.getAttributeName());
            record.setValue("ATTRIBUTE_DESCRIPTION", definition.getAttributeDescription());

            // Additional fields based on feed definition
            record.setValue("CHECKER_SOEID", "CHECKER" + i);
            record.setValue("MAKER_SOEID", "MAKER" + i);
            record.setValue("ORIGINAL_CSI", definition.getSourceSystemCsi());
            record.setValue("ADJUSTMENT_POSTING_STATUS", "Posted");
            record.setValue("TRANSACTION_CCY", "USD");
            record.setValue("ADJUSTMENT_METHOD", "Manual");
            record.setValue("EVENT_STATUS", "Closed");
            record.setValue("RECURRENCE", "Recurring");
            record.setValue("ADJUSTING_SYSTEM", definition.getSourceSystemCsi());
            record.setValue("FREQUENCY", "Daily");
            record.setValue("STANDARD_ACCOUNT", "NA");
            record.setValue("FRS_BU", "Undetermined");
            record.setValue("BALANCE_TYPE", "NA");

            // Trade-specific fields
            if ("trade".equalsIgnoreCase(definition.getRecordType())) {
                record.setValue("UITID", "UITID" + String.format("%08d", i));
                record.setValue("CDE_FLAG", "Y");
            }

            // Additional fields
            for (int j = 1; j <= 4; j++) {
                record.setValue("DETAIL_ATTRIBUTE" + j, "");
            }
            record.setValue("JOURNAL_UPLOAD_DESCRIPTION", "Sample description for record " + i);

            records.add(record);
        }

        return records;
    }
}
