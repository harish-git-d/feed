package com.ccc.risk.credit.gai.config;

import com.ccc.risk.credit.gai.listener.FeedJobExecutionListener;
import com.ccc.risk.credit.gai.listener.SftpRetryStepListener;
import com.ccc.risk.credit.gai.service.DatabaseQueryService;
import com.ccc.risk.credit.gai.service.FeedFileNamingService;
import com.ccc.risk.credit.gai.service.FileWriterService;
import com.ccc.risk.credit.gai.tasklet.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the {@code gaiFeedJob} Spring Batch job.
 *
 * <p>Step sequence:
 * <ol>
 *   <li>loadDefinitionStep    — loads feed YAML from classpath</li>
 *   <li>generateEventStep     — queries DB, writes EVENT .dat.gz</li>
 *   <li>generateRecordStep    — queries DB, writes RECORD .dat.gz</li>
 *   <li>generateAttributeStep — queries DB, writes ATTRIBUTE .dat.gz</li>
 *   <li>generateControlStep   — writes CONTROL .dat.gz (GAI trigger)</li>
 *   <li>transferFilesStep     — SFTP: data files first, control file last</li>
 * </ol>
 *
 * <p>Exception handling:
 * <ul>
 *   <li>{@link FeedJobExecutionListener} — cleans up partial files and sends
 *       email alert on job failure</li>
 *   <li>{@link SftpRetryStepListener} — logs retry hint on SFTP step failure</li>
 *   <li>{@link com.ccc.risk.credit.gai.service.SftpTransferService} — retries
 *       transfer up to 3 times with exponential backoff</li>
 * </ul>
 */
@Configuration
public class JobConfig {

    @Bean
    public Job gaiFeedJob(JobRepository jobRepository,
                          FeedJobExecutionListener jobListener,
                          Step loadDefinitionStep,
                          Step generateEventStep,
                          Step generateRecordStep,
                          Step generateAttributeStep,
                          Step generateControlStep,
                          Step transferFilesStep) {
        return new JobBuilder("gaiFeedJob", jobRepository)
                .listener(jobListener)          // cleanup + email on failure
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
                                  DatabaseQueryService dbService,
                                  FeedFileNamingService namingService,
                                  FileWriterService writerService,
                                  FeedProperties feedProperties) {
        return new StepBuilder("generateEventStep", jobRepository)
                .tasklet(new GenerateCategoryFileTasklet(
                        namingService, writerService, dbService, feedProperties, "EVENT"),
                         transactionManager)
                .build();
    }

    @Bean
    public Step generateRecordStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   DatabaseQueryService dbService,
                                   FeedFileNamingService namingService,
                                   FileWriterService writerService,
                                   FeedProperties feedProperties) {
        return new StepBuilder("generateRecordStep", jobRepository)
                .tasklet(new GenerateCategoryFileTasklet(
                        namingService, writerService, dbService, feedProperties, "RECORD"),
                         transactionManager)
                .build();
    }

    @Bean
    public Step generateAttributeStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      DatabaseQueryService dbService,
                                      FeedFileNamingService namingService,
                                      FileWriterService writerService,
                                      FeedProperties feedProperties) {
        return new StepBuilder("generateAttributeStep", jobRepository)
                .tasklet(new GenerateCategoryFileTasklet(
                        namingService, writerService, dbService, feedProperties, "ATTRIBUTE"),
                         transactionManager)
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
                                  TransferFilesTasklet tasklet,
                                  SftpRetryStepListener sftpListener) {
        return new StepBuilder("transferFilesStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(sftpListener)         // logs retry hint on SFTP failure
                .build();
    }
}
