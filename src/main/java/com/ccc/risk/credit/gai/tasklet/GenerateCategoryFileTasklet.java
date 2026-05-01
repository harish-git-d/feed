package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import com.ccc.risk.credit.gai.service.DatabaseQueryService;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates one data file (EVENT, RECORD, or ATTRIBUTE) for the active feed.
 *
 * <p>Exception handling:
 * <ul>
 *   <li>Missing {@code feedDefinition} context → hard fail with clear message</li>
 *   <li>DB query failure → wrapped with feed/category context and rethrown</li>
 *   <li>File write failure → wrapped with feed/category context and rethrown</li>
 * </ul>
 *
 * <p>On any failure the {@link FeedJobExecutionListener} (job-level) will
 * delete all partially-written files from this run.
 */
@Slf4j
public class GenerateCategoryFileTasklet implements Tasklet {

    private final FeedFileNamingService fileNamingService;
    private final FileWriterService     fileWriterService;
    private final DatabaseQueryService  databaseQueryService;
    private final FeedProperties        feedProperties;
    private final String                category;

    public GenerateCategoryFileTasklet(
            FeedFileNamingService fileNamingService,
            FileWriterService fileWriterService,
            DatabaseQueryService databaseQueryService,
            FeedProperties feedProperties,
            String category) {
        this.fileNamingService    = fileNamingService;
        this.fileWriterService    = fileWriterService;
        this.databaseQueryService = databaseQueryService;
        this.feedProperties       = feedProperties;
        this.category             = category;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        String feedName = feedProperties.getFeedName();
        String cobDate  = feedProperties.getCobDate();

        log.info("[{}][{}] Generating {} file for cobDate={}",
                feedName, category, category, cobDate);

        ExecutionContext jobCtx = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

        // --- Retrieve feed definition ---
        FeedDefinition feedDefinition = (FeedDefinition) jobCtx.get("feedDefinition");
        if (feedDefinition == null) {
            throw new IllegalStateException(
                "feedDefinition not found in JobExecutionContext. "
                + "Ensure LoadFeedDefinitionTasklet ran before this step.");
        }

        // --- Query database ---
        List<FeedRecord> records;
        try {
            records = databaseQueryService.executeQuery(feedDefinition, category, cobDate);
        } catch (Exception e) {
            throw new IllegalStateException(
                String.format("[%s][%s][cobDate=%s] Database query failed: %s",
                        feedName, category, cobDate, e.getMessage()), e);
        }

        log.info("[{}][{}] Retrieved {} records from DB", feedName, category, records.size());

        // --- Write file ---
        String fileName = fileNamingService.buildDataFileName(feedDefinition, category);
        FileMetadata metadata;
        try {
            metadata = fileWriterService.writeDataFile(fileName, category, feedDefinition, records);
        } catch (Exception e) {
            throw new IllegalStateException(
                String.format("[%s][%s] File write failed for '%s': %s",
                        feedName, category, fileName, e.getMessage()), e);
        }

        log.info("[{}][{}] File written: {} ({} records)", feedName, category, fileName, records.size());

        // --- Accumulate deliveredFiles for control + SFTP steps ---
        List<FileMetadata> deliveredFiles =
                (List<FileMetadata>) jobCtx.get("deliveredFiles");
        if (deliveredFiles == null) {
            deliveredFiles = new ArrayList<>();
            jobCtx.put("deliveredFiles", deliveredFiles);
        }
        deliveredFiles.add(metadata);

        return RepeatStatus.FINISHED;
    }
}
