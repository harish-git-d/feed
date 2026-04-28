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
 * Data files are gzip-compressed when {@code gai.gzip-enabled=true} (default).
 *
 * <p>Control file format (.ctrl — plain text, never gzipped):
 * <pre>
 *   T|{cobDate}|{totalRecordCount}
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

        long recordCount = 0;

        try (BufferedWriter writer = openGzipWriter(outputPath)) {
            writer.write(buildHeader(definition, fileType));
            writer.newLine();

            for (FeedRecord record : records) {
                writer.write(toDetailLine(definition, record, fileType));
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

        // Plain text — T|cobDate|totalRecordCount
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

    private String toDetailLine(FeedDefinition definition, FeedRecord record, String fileType) {
        List<String> fields = getFieldsForType(definition, fileType);
        StringBuilder sb = new StringBuilder("D");
        for (String field : fields) {
            sb.append(definition.getDelimiter());
            String val = record.getValue(field);
            sb.append(val != null ? val.replace("|", "\\|") : "");
        }
        return sb.toString();
    }

    /**
     * Returns the correct field list based on file type (EVENT / RECORD / ATTRIBUTE).
     * Falls back to recordFields for backward compatibility.
     */
    private List<String> getFieldsForType(FeedDefinition definition, String fileType) {
        return switch (fileType.toUpperCase()) {
            case "EVENT"     -> definition.getEventFields()     != null
                                ? definition.getEventFields()
                                : definition.getRecordFields();
            case "RECORD"    -> definition.getRecordFields();
            case "ATTRIBUTE" -> definition.getAttributeFields() != null
                                ? definition.getAttributeFields()
                                : definition.getRecordFields();
            default          -> definition.getRecordFields();
        };
    }

    /** Opens a gzip-compressed writer for data files. */
    private BufferedWriter openGzipWriter(Path path) throws IOException {
        OutputStream fileStream = Files.newOutputStream(path);
        OutputStream out = properties.isGzipEnabled()
                ? new GZIPOutputStream(new BufferedOutputStream(fileStream, 8192))
                : new BufferedOutputStream(fileStream, 8192);
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    /** Opens a plain (non-gzipped) writer for control files. */
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
        try { return Files.size(path); }
        catch (IOException e) { return 0L; }
    }

    private String normaliseCobDate() {
        return properties.getCobDate().replace("-", "");
    }
}
