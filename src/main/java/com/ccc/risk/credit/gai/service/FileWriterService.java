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
import java.util.List;
import java.util.Map;
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
 * <p>Control file format:
 * <pre>
 *   T|{cobDate}|{totalRecordCount}
 * </pre>
 *
 * <p>All files are optionally gzip-compressed (controlled by
 * {@code gai.gzip-enabled}, default {@code true}).
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

        long recordCount = 0;

        try (BufferedWriter writer = openWriter(outputPath)) {

            // Header: H|field1|field2|...
            writer.write(buildHeader(definition));
            writer.newLine();

            // Detail rows: D|val1|val2|...
            for (FeedRecord record : records) {
                writer.write(toDetailLine(definition, record));
                writer.newLine();
                recordCount++;
            }

            // Trailer: T|YYYYMMDD|recordCount
            writer.write(buildTrailer(definition, recordCount));
            writer.newLine();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + fileType + " file: " + outputPath, e);
        }

        long fileSize = safeFileSize(outputPath);
        log.info("Wrote {} file: {} ({} records, {} bytes)", fileType, fileName, recordCount, fileSize);

        return new FileMetadata(fileName, fileType, recordCount, fileSize, outputPath.toString());
    }

    // -------------------------------------------------------------------------
    // Control file
    // -------------------------------------------------------------------------

    /**
     * Writes the GAI control/trigger file.
     *
     * <p>Format: {@code T|{cobDate}|{totalRecordCount}}
     * where totalRecordCount is the sum of records across all delivered data files.
     * The control file is sent last — GAI uses it as the processing trigger.
     */
    public FileMetadata writeControlFile(String fileName, FeedDefinition definition,
                                          List<FileMetadata> dataFiles) {
        Path outputPath = resolveOutputPath(fileName);
        ensureDirectoryExists(outputPath.getParent());

        long totalRecords = dataFiles.stream()
                .mapToLong(FileMetadata::getRecordCount)
                .sum();

        // T|cobDate|totalRecordCount  (matches GAI 2.0 spec trailer format)
        String content = "T" + definition.getDelimiter()
                       + normaliseCobDate() + definition.getDelimiter()
                       + totalRecords + "\n";

        try (BufferedWriter writer = openWriter(outputPath)) {
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

    private String buildHeader(FeedDefinition definition) {
        // H|field1|field2|field3|...
        List<String> fields = definition.getRecordFields();
        return "H" + definition.getDelimiter()
               + String.join(definition.getDelimiter(), fields);
    }

    private String buildTrailer(FeedDefinition definition, long count) {
        // T|cobDate|recordCount
        return "T" + definition.getDelimiter()
               + normaliseCobDate() + definition.getDelimiter()
               + count;
    }

    private String toDetailLine(FeedDefinition definition, FeedRecord record) {
        StringBuilder sb = new StringBuilder("D");
        for (String field : definition.getRecordFields()) {
            sb.append(definition.getDelimiter());
            String val = record.getValue(field);
            // Escape embedded pipe characters
            sb.append(val != null ? val.replace("|", "\\|") : "");
        }
        return sb.toString();
    }

    private BufferedWriter openWriter(Path path) throws IOException {
        OutputStream fileStream = Files.newOutputStream(path);
        OutputStream out = properties.isGzipEnabled()
                ? new GZIPOutputStream(new BufferedOutputStream(fileStream, 8192))
                : new BufferedOutputStream(fileStream, 8192);
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private Path resolveOutputPath(String fileName) {
        return Paths.get(properties.getOutputDirectory(), fileName);
    }

    private void ensureDirectoryExists(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create output directory: " + directory, e);
        }
    }

    private long safeFileSize(Path path) {
        try { return Files.size(path); }
        catch (IOException e) { return 0L; }
    }

    /** Normalises cobDate from YYYY-MM-DD to YYYYMMDD for use in file content. */
    private String normaliseCobDate() {
        return properties.getCobDate().replace("-", "");
    }

    private String extractBusinessUnit() {
        String appShortName = properties.getAppShortName();
        return appShortName.contains("_") ? appShortName.split("_")[0] : appShortName;
    }
}
