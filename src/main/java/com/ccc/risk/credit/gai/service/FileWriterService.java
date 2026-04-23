package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class FileWriterService {

    private final FeedProperties properties;

    public FileWriterService(FeedProperties properties) {
        this.properties = properties;
    }

    /**
     * Writes a data file (EVENT, RECORD, or ATTRIBUTE) with proper formatting
     */
    public FileMetadata writeDataFile(String fileName, String fileType, FeedDefinition definition, List<FeedRecord> records) {
        Path outputPath = Paths.get(properties.getOutputDirectory(), fileName);
        createDirectories(outputPath.getParent());

        long recordCount = 0;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(openOutputStream(outputPath), StandardCharsets.UTF_8))) {

            // Write header
            writer.write(buildHeader(definition));
            writer.newLine();

            // Write detail records
            for (FeedRecord record : records) {
                writer.write(toLine(definition, record));
                writer.newLine();
                recordCount++;
            }

            // Write trailer
            writer.write(buildTrailer(definition, recordCount));
            writer.newLine();

        } catch (IOException e) {
            log.error("Failed to write {} file: {}", fileType, outputPath, e);
            throw new IllegalStateException("Failed to write file " + outputPath, e);
        }

        long fileSize = getFileSize(outputPath);
        log.info("Successfully wrote {} file: {} ({} records, {} bytes)",
                fileType, fileName, recordCount, fileSize);

        return new FileMetadata(fileName, fileType, recordCount, fileSize, outputPath.toString());
    }

    /**
     * Writes the control file in XML format
     */
    public FileMetadata writeControlFile(String fileName, FeedDefinition definition, List<FileMetadata> dataFiles) {
        Path outputPath = Paths.get(properties.getOutputDirectory(), fileName);
        createDirectories(outputPath.getParent());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(openOutputStream(outputPath), StandardCharsets.UTF_8))) {

            String xmlContent = buildControlFileXml(definition, dataFiles);
            writer.write(xmlContent);

        } catch (IOException e) {
            log.error("Failed to write control file: {}", outputPath, e);
            throw new IllegalStateException("Failed to write control file " + outputPath, e);
        }

        long fileSize = getFileSize(outputPath);
        log.info("Successfully wrote control file: {} ({} bytes)", fileName, fileSize);

        return new FileMetadata(fileName, "CONTROL", dataFiles.size(), fileSize, outputPath.toString());
    }

    /**
     * Builds the XML content for the control file
     */
    private String buildControlFileXml(FeedDefinition definition, List<FileMetadata> dataFiles) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<root>\n");
        xml.append("  <header>\n");
        xml.append("    <source_system>").append(properties.getSourceSystemName()).append("</source_system>\n");
        xml.append("    <csi>").append(properties.getSourceSystemCsi()).append("</csi>\n");
        xml.append("    <business_unit>").append(extractBusinessUnit()).append("</business_unit>\n");
        xml.append("    <feed_name>").append(definition.getFeedName()).append("</feed_name>\n");
        xml.append("    <module_name>").append(definition.getModuleName()).append("</module_name>\n");
        xml.append("    <cob_date>").append(formatCobDate()).append("</cob_date>\n");
        xml.append("    <timestamp>").append(properties.getFileTimestamp()).append("</timestamp>\n");
        xml.append("    <frequency>").append(properties.getFrequency()).append("</frequency>\n");
        xml.append("    <generation_time>").append(getCurrentTimestamp()).append("</generation_time>\n");
        xml.append("  </header>\n");
        xml.append("  <files>\n");

        for (FileMetadata file : dataFiles) {
            xml.append("    <file>\n");
            xml.append("      <type>").append(file.getFileType()).append("</type>\n");
            xml.append("      <name>").append(file.getFileName()).append("</name>\n");
            xml.append("      <record_count>").append(file.getRecordCount()).append("</record_count>\n");
            xml.append("      <file_size_bytes>").append(file.getFileSizeBytes()).append("</file_size_bytes>\n");
            xml.append("    </file>\n");
        }

        xml.append("  </files>\n");
        xml.append("  <summary>\n");
        xml.append("    <total_files>").append(dataFiles.size()).append("</total_files>\n");
        xml.append("    <total_records>").append(dataFiles.stream().mapToLong(FileMetadata::getRecordCount).sum()).append("</total_records>\n");
        xml.append("    <total_size_bytes>").append(dataFiles.stream().mapToLong(FileMetadata::getFileSizeBytes).sum()).append("</total_size_bytes>\n");
        xml.append("  </summary>\n");
        xml.append("</root>\n");

        return xml.toString();
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create output directory " + directory, e);
        }
    }

    private OutputStream openOutputStream(Path path) throws IOException {
        OutputStream fileStream = Files.newOutputStream(path);
        return properties.isGzipEnabled() ? new GZIPOutputStream(fileStream) : fileStream;
    }

    private String buildHeader(FeedDefinition definition) {
        // Header format: H|field1|field2|field3|...
        return "H" + definition.getDelimiter() + String.join(definition.getDelimiter(), definition.getRecordFields());
    }

    private String buildTrailer(FeedDefinition definition, long count) {
        // Trailer format: T|COBDATE|RecordCount
        return "T" + definition.getDelimiter() + formatCobDate() + definition.getDelimiter() + count;
    }

    private String toLine(FeedDefinition definition, FeedRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("D"); // Detail record indicator

        for (String field : definition.getRecordFields()) {
            builder.append(definition.getDelimiter());
            builder.append(valueOrEmpty(record.getValues(), field));
        }

        return builder.toString();
    }

    private String valueOrEmpty(Map<String, String> values, String key) {
        return values.getOrDefault(key, "");
    }

    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            log.warn("Could not determine file size for: {}", path, e);
            return 0L;
        }
    }

    private String formatCobDate() {
        // Convert YYYY-MM-DD to YYYYMMDD
        return properties.getCobDate().replace("-", "");
    }

    private String extractBusinessUnit() {
        // Extract "CRC" from "CRC_SCEF"
        String appShortName = properties.getAppShortName();
        return appShortName.contains("_") ? appShortName.split("_")[0] : appShortName;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}