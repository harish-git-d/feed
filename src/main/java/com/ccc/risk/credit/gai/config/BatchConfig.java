package com.ccc.risk.credit.gai.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration — fully in-memory, zero database tables.
 *
 * <p>Uses a {@link InMemoryJobRepository} backed by {@link java.util.concurrent.ConcurrentHashMap}
 * and a {@link ResourcelessTransactionManager} (no-op transaction manager).
 * No H2, no Oracle DDL, no schema scripts required at all.
 *
 * <p>Note: {@code @EnableBatchProcessing} is intentionally omitted.
 * In Spring Batch 5 it disables Boot auto-configuration. We instead
 * manually declare the {@link JobRepository}, {@link PlatformTransactionManager},
 * and {@link JobLauncher} beans here, which Spring Batch picks up automatically.
 */
@Configuration
public class BatchConfig {

    /**
     * In-memory job repository — no database tables whatsoever.
     * All Spring Batch metadata stored in ConcurrentHashMaps.
     */
    @Bean
    public JobRepository jobRepository() {
        return new InMemoryJobRepository();
    }

    /**
     * No-op transaction manager — since there is no database backing
     * the job repository, real transactions are not needed for Batch metadata.
     * SCEF Oracle queries use their own {@code scefTransactionManager}
     * defined in {@link DataSourceConfig}.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    /**
     * Synchronous job launcher — runs the job on the calling thread.
     * {@link System#exit} in {@code FeedBatchApplication} waits for
     * the job to complete before exiting, so synchronous is correct here.
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }
}
