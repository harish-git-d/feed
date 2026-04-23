package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import com.ccc.risk.credit.gai.service.SampleDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope
public class GenerateCategoryFileTasklet implements Tasklet {

    private final FeedFileNamingService fileNamingService;
    private final FileWriterService fileWriterService;
    private final SampleDataFactory sampleDataFactory;
    private final String category;

    @Value("#{jobExecutionContext['feedDefinition']}")
    private FeedDefinition feedDefinition;

    public GenerateCategoryFileTasklet(
            FeedFileNamingService fileNamingService,
            FileWriterService fileWriterService,
            SampleDataFactory sampleDataFactory,
            @Value("${file.category:EVENT}") String category) {
        this.fileNamingService = fileNamingService;
        this.fileWriterService = fileWriterService;
        this.sampleDataFactory = sampleDataFactory;
        this.category = category;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Generating {} file for feed: {}", category, feedDefinition.getFeedName());

        // Generate sample data (replace with actual DB query in production)
        List<FeedRecord> records = sampleDataFactory.generateRecords(feedDefinition, category);

        // Build file name
        String fileName = fileNamingService.buildDataFileName(feedDefinition, category);

        // Write the file
        FileMetadata fileMetadata = fileWriterService.writeDataFile(fileName, category, feedDefinition, records);

        // Store metadata in job execution context
        @SuppressWarnings("unchecked")
        List<FileMetadata> deliveredFiles = (List<FileMetadata>) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("deliveredFiles");

        if (deliveredFiles == null) {
            deliveredFiles = new ArrayList<>();
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("deliveredFiles", deliveredFiles);
        }

        deliveredFiles.add(fileMetadata);

        log.info("Successfully generated {} file: {} with {} records",
                category, fileName, fileMetadata.getRecordCount());

        return RepeatStatus.FINISHED;
    }
}