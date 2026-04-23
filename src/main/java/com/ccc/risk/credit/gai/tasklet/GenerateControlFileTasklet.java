package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@StepScope
public class GenerateControlFileTasklet implements Tasklet {

    private final FeedFileNamingService fileNamingService;
    private final FileWriterService fileWriterService;

    public GenerateControlFileTasklet(
            FeedFileNamingService fileNamingService,
            FileWriterService fileWriterService) {
        this.fileNamingService = fileNamingService;
        this.fileWriterService = fileWriterService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // Retrieve feed definition from job execution context
        FeedDefinition definition = (FeedDefinition) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("feedDefinition");

        // Retrieve list of delivered files
        List<FileMetadata> deliveredFiles = (List<FileMetadata>) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("deliveredFiles");

        if (deliveredFiles == null || deliveredFiles.isEmpty()) {
            log.warn("No data files were generated. Control file will not be created.");
            return RepeatStatus.FINISHED;
        }

        log.info("Generating control file for {} data files", deliveredFiles.size());

        // Build control file name
        String controlFileName = fileNamingService.buildControlFileName(definition);

        // Write the control file
        FileMetadata controlFileMetadata = fileWriterService.writeControlFile(
                controlFileName, definition, deliveredFiles);

        log.info("Successfully generated control file: {}", controlFileName);

        // Store control file metadata
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("controlFile", controlFileMetadata);

        return RepeatStatus.FINISHED;
    }
}
