package com.ccc.risk.credit.gai.config;

import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import com.ccc.risk.credit.gai.service.SampleDataFactory;
import com.ccc.risk.credit.gai.tasklet.GenerateCategoryFileTasklet;
import com.ccc.risk.credit.gai.tasklet.GenerateControlFileTasklet;
import com.ccc.risk.credit.gai.tasklet.LoadFeedDefinitionTasklet;
import com.ccc.risk.credit.gai.tasklet.TransferFilesTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobConfig {

    @Bean
    public Job gaiFeedJob(JobRepository jobRepository,
                          Step loadDefinitionStep,
                          Step generateEventStep,
                          Step generateRecordStep,
                          Step generateAttributeStep,
                          Step generateControlStep,
                          Step transferFilesStep) {
        return new JobBuilder("gaiFeedJob", jobRepository)
                .start(loadDefinitionStep)
                .next(generateEventStep)
                .next(generateRecordStep)
                .next(generateAttributeStep)
                .next(generateControlStep)
                .next(transferFilesStep)
                .build();
    }

    @Bean
    public Step loadDefinitionStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   LoadFeedDefinitionTasklet tasklet) {
        return new StepBuilder("loadDefinitionStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step generateEventStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  GenerateCategoryFileTasklet eventTasklet) {
        return new StepBuilder("generateEventStep", jobRepository)
                .tasklet(eventTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step generateRecordStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   GenerateCategoryFileTasklet recordTasklet) {
        return new StepBuilder("generateRecordStep", jobRepository)
                .tasklet(recordTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step generateAttributeStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      GenerateCategoryFileTasklet attributeTasklet) {
        return new StepBuilder("generateAttributeStep", jobRepository)
                .tasklet(attributeTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step generateControlStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    GenerateControlFileTasklet tasklet) {
        return new StepBuilder("generateControlStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step transferFilesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  TransferFilesTasklet tasklet) {
        return new StepBuilder("transferFilesStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateCategoryFileTasklet eventTasklet(SampleDataFactory sampleDataFactory,
                                                    FeedFileNamingService fileNamingService,
                                                    FileWriterService fileWriterService) {
        return createCategoryFileTasklet(sampleDataFactory, fileNamingService, fileWriterService, "EVENT");
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateCategoryFileTasklet recordTasklet(SampleDataFactory sampleDataFactory,
                                                     FeedFileNamingService fileNamingService,
                                                     FileWriterService fileWriterService) {
        return createCategoryFileTasklet(sampleDataFactory, fileNamingService, fileWriterService, "RECORD");
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateCategoryFileTasklet attributeTasklet(SampleDataFactory sampleDataFactory,
                                                        FeedFileNamingService fileNamingService,
                                                        FileWriterService fileWriterService) {
        return createCategoryFileTasklet(sampleDataFactory, fileNamingService, fileWriterService, "ATTRIBUTE");
    }

    private GenerateCategoryFileTasklet createCategoryFileTasklet(SampleDataFactory sampleDataFactory,
                                                                  FeedFileNamingService fileNamingService,
                                                                  FileWriterService fileWriterService,
                                                                  String category) {
        return new GenerateCategoryFileTasklet(
                fileNamingService, fileWriterService, sampleDataFactory, category);
    }
}
