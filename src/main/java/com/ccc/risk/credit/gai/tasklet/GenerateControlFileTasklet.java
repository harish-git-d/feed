package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates the GAI ADIL control/trigger file once all three data files
 * (EVENT, RECORD, ATTRIBUTE) have been written successfully.
 *
 * <p>Control file format per GAI 2.0 spec:
 * <pre>
 *   T|{cobDate}|{totalRecordCount}
 * </pre>
 *
 * <p>Fails fast with a clear message if the {@code deliveredFiles} context
 * entry is missing, which would indicate a preceding step didn't complete.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateControlFileTasklet implements Tasklet {

    private final FeedFileNamingService fileNamingService;
    private final FileWriterService     fileWriterService;

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        ExecutionContext jobCtx = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

        FeedDefinition definition = (FeedDefinition) jobCtx.get("feedDefinition");
        if (definition == null) {
            throw new IllegalStateException(
                "feedDefinition not found in JobExecutionContext. "
                + "Ensure LoadFeedDefinitionTasklet completed successfully.");
        }

        List<FileMetadata> deliveredFiles = (List<FileMetadata>) jobCtx.get("deliveredFiles");
        if (deliveredFiles == null || deliveredFiles.isEmpty()) {
            // Treat as a hard failure — if no data files were written something
            // went wrong upstream that Spring Batch should already have caught.
            throw new IllegalStateException(
                "No delivered files found in JobExecutionContext for feed '"
                + definition.getFeedName() + "'. "
                + "At least one data file (EVENT/RECORD/ATTRIBUTE) must have been "
                + "written before generating the control file.");
        }

        log.info("Generating control file for {} data file(s) [feed={}]",
                deliveredFiles.size(), definition.getFeedName());

        try {
            String controlFileName = fileNamingService.buildControlFileName(definition);
            FileMetadata controlMeta = fileWriterService.writeControlFile(
                    controlFileName, definition, deliveredFiles);

            log.info("Control file written: {} (totalRecords={})",
                    controlFileName, controlMeta.getRecordCount());

            // Add to deliveredFiles so TransferFilesTasklet sends it last (as GAI trigger)
            deliveredFiles.add(controlMeta);
            jobCtx.put("controlFile", controlMeta);

        } catch (IllegalStateException e) {
            // Already has context — rethrow with feed name prepended for clarity
            throw new IllegalStateException(
                "[" + definition.getFeedName() + "] Control file generation failed: "
                + e.getMessage(), e.getCause());
        }

        return RepeatStatus.FINISHED;
    }
}
