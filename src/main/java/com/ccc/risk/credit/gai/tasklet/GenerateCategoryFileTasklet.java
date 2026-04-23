package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.domain.FeedRecord;
import com.ccc.risk.credit.gai.service.DatabaseQueryService;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GenerateCategoryFileTasklet implements Tasklet {

    private final FeedFileNamingService fileNamingService;
    private final FileWriterService fileWriterService;
    private final DatabaseQueryService databaseQueryService;
    private final FeedProperties feedProperties;
    private final String category;

    public GenerateCategoryFileTasklet(
            FeedFileNamingService fileNamingService,
            FileWriterService fileWriterService,
            DatabaseQueryService databaseQueryService,
            FeedProperties feedProperties,
            @Value("${file.category:EVENT}") String category) {
        this.fileNamingService = fileNamingService;
        this.fileWriterService = fileWriterService;
        this.databaseQueryService = databaseQueryService;
        this.feedProperties = feedProperties;
        this.category = category;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Generating {} file", category);

        // Get feed definition from job execution context
        FeedDefinition feedDefinition = (FeedDefinition) chunkContext.getStepContext()
                .getJobExecutionContext()
                .get("feedDefinition");

        // Query database for records
        List<FeedRecord> records = databaseQueryService.executeQuery(
                feedDefinition,
                category,
                feedProperties.getCobDate()
        );

        // Generate file name
        String fileName = fileNamingService.generateDataFileName(category);

        // Write records to file
        fileWriterService.writeDataFile(fileName, records, feedDefinition);

        log.info("Generated {} file with {} records: {}", category, records.size(), fileName);

        // Store file name in context for control file generation
        chunkContext.getStepContext()
                .getJobExecutionContext()
                .put(category.toLowerCase() + "FileName", fileName);
        chunkContext.getStepContext()
                .getJobExecutionContext()
                .put(category.toLowerCase() + "RecordCount", records.size());

        return RepeatStatus.FINISHED;
    }
}