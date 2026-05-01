package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Writes GAI ADIL-format feed files to the configured output directory.
 *
 * <p>Data file format (EVENT / RECORD / ATTRIBUTE):
 * <pre>
 *   H|col1|col2|col3          ← header row
 *   D|val1|val2|val3          ← one detail row per record
 *   T|{cobDate}|{count}       ← trailer row
 * </pre>
 *
 * <p>Control file format (.ctrl — plain text, never gzipped):
 * <pre>
 *   T|{cobDate}|{totalRecordCount}
 * </pre>
 *
 * <p><b>Date formatting:</b> Columns listed in {@link FeedDefinition#getDateColumns()}
 * are formatted using the pattern configured at {@code gai.date-format}
 * (default {@code MMddyyyy}). Change the format in application YAML without
 * any code changes:
 * <pre>
 *   gai:
 *     date-format: MMddyyyy        # → 06122024
 *     # date-format: yyyyMMdd      # → 20240612
 *     # date-format: MM/dd/yyyy    # → 06/12/2024
 *     # date-format: yyyy-MM-dd    # → 2024-06-12
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileWriterService {

    private final FeedProperties properties;

    // -------------------------------------------------------------------------
    // Data file (EVENT / RECORD / ATTRIBUTE)
    // -------------------------------------------------------------------------

    public FileMetadata writeDataFile(String fileName, String fileType,
                                      FeedDefinition definition, List<FeedRecord> records) {
        Path outputPath = resolveOutputPath(fileName);
        ensureDirectoryExists(outputPath.getParent());

        SimpleDateFormat sdf = buildDateFormatter();
        long recordCount = 0;

        try (BufferedWriter writer = openGzipWriter(outputPath)) {
            writer.write(buildHeader(definition, fileType));
            writer.newLine();

            for (FeedRecord record : records) {
                writer.write(toDetailLine(definition, record, fileType, sdf));
                writer.newLine();
                recordCount++;
            }

            writer.write(buildTrailer(definition, recordCount));
            writer.newLine();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to write " + fileType + " file: " + outputPath, e);
        }

        long fileSize = safeFileSize(outputPath);
        log.info("Wrote {} file: {} ({} records, {} bytes)", fileType, fileName, recordCount, fileSize);
        return new FileMetadata(fileName, fileType, recordCount, fileSize, outputPath.toString());
    }

    // -------------------------------------------------------------------------
    // Control file — plain text .ctrl, never gzipped
    // -------------------------------------------------------------------------

    public FileMetadata writeControlFile(String fileName, FeedDefinition definition,
                                          List<FileMetadata> dataFiles) {
        Path outputPath = resolveOutputPath(fileName);
        ensureDirectoryExists(outputPath.getParent());

        long totalRecords = dataFiles.stream()
                .mapToLong(FileMetadata::getRecordCount)
                .sum();

        String content = "T" + definition.getDelimiter()
                       + normaliseCobDate() + definition.getDelimiter()
                       + totalRecords + "\n";

        try (BufferedWriter writer = openPlainWriter(outputPath)) {
            writer.write(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write control file: " + outputPath, e);
        }

        long fileSize = safeFileSize(outputPath);
        log.info("Wrote CONTROL file: {} (totalRecords={}, {} bytes)", fileName, totalRecords, fileSize);
        return new FileMetadata(fileName, "CONTROL", totalRecords, fileSize, outputPath.toString());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildHeader(FeedDefinition definition, String fileType) {
        List<String> fields = getFieldsForType(definition, fileType);
        return "H" + definition.getDelimiter()
               + String.join(definition.getDelimiter(), fields);
    }

    private String buildTrailer(FeedDefinition definition, long count) {
        return "T" + definition.getDelimiter()
               + normaliseCobDate() + definition.getDelimiter()
               + count;
    }

    private String toDetailLine(FeedDefinition definition, FeedRecord record,
                                String fileType, SimpleDateFormat sdf) {
        List<String> fields   = getFieldsForType(definition, fileType);
        Set<String>  dateCols = definition.getDateColumns();

        StringBuilder sb = new StringBuilder("D");
        for (String field : fields) {
            sb.append(definition.getDelimiter());
            String val = record.getValue(field);
            // Apply date formatting if this column is a date column and has a value
            if (dateCols != null && dateCols.contains(field)
                    && val != null && !val.isBlank() && !val.equals("NA")) {
                val = formatDateValue(val, sdf);
            }
            if (val != null) {
                sb.append(val.replace("|", "\\|"));
            }
        }
        return sb.toString();
    }

    /**
     * Attempts to parse a raw date/timestamp string and reformat it using
     * the configured pattern. Falls back to the raw value if parsing fails.
     *
     * <p>Oracle JDBC returns timestamps as strings like:
     * {@code 2024-06-12 11:40:55.917} or {@code 2024-06-12 00:00:00.0}
     */
    private String formatDateValue(String raw, SimpleDateFormat targetFmt) {
        // Patterns Oracle JDBC commonly returns
        String[] oraclePatterns = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy"
        };
        for (String pattern : oraclePatterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern);
                parser.setLenient(false);
                Date parsed = parser.parse(raw);
                return targetFmt.format(parsed);
            } catch (Exception ignored) {
                // try next pattern
            }
        }
        // Could not parse — return raw value as-is
        log.debug("Could not parse date value '{}' — writing as-is", raw);
        return raw;
    }

    private SimpleDateFormat buildDateFormatter() {
        String pattern = properties.getDateFormat();
        try {
            return new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date-format '{}' — falling back to MMddyyyy", pattern);
            return new SimpleDateFormat("MMddyyyy");
        }
    }

    private List<String> getFieldsForType(FeedDefinition definition, String fileType) {
        return switch (fileType.toUpperCase()) {
            case "EVENT"     -> definition.getEventFields()     != null
                                ? definition.getEventFields()     : definition.getRecordFields();
            case "RECORD"    -> definition.getRecordFields();
            case "ATTRIBUTE" -> definition.getAttributeFields() != null
                                ? definition.getAttributeFields() : definition.getRecordFields();
            default          -> definition.getRecordFields();
        };
    }

    private BufferedWriter openGzipWriter(Path path) throws IOException {
        OutputStream fileStream = Files.newOutputStream(path);
        OutputStream out = properties.isGzipEnabled()
                ? new GZIPOutputStream(new BufferedOutputStream(fileStream, 8192))
                : new BufferedOutputStream(fileStream, 8192);
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private BufferedWriter openPlainWriter(Path path) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(
                new BufferedOutputStream(Files.newOutputStream(path), 8192),
                StandardCharsets.UTF_8));
    }

    private Path resolveOutputPath(String fileName) {
        return Paths.get(properties.getOutputDirectory(), fileName);
    }

    private void ensureDirectoryExists(Path directory) {
        try { Files.createDirectories(directory); }
        catch (IOException e) {
            throw new IllegalStateException("Unable to create output directory: " + directory, e);
        }
    }

    private long safeFileSize(Path path) {
        try { return Files.size(path); } catch (IOException e) { return 0L; }
    }

    private String normaliseCobDate() {
        return properties.getCobDate().replace("-", "");
    }
}
