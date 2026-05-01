package com.ccc.risk.credit.gai;

import com.ccc.risk.credit.gai.config.FeedProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the SCEF GAI Feed application.
 *
 * <p>Launches {@code gaiFeedJob} for the configured feed and exits with
 * a non-zero status code on failure so schedulers (cron, Control-M, etc.)
 * detect the failure correctly.
 *
 * <pre>
 *   java -jar gai-batch-project.jar \
 *       --gai.feed-name=stress-exposure \
 *       --gai.cob-date=20251231 \
 *       --gai.file-timestamp=20260122144002
 * </pre>
 */
@Slf4j
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration.class
})
@EnableConfigurationProperties(FeedProperties.class)
@RequiredArgsConstructor
public class FeedBatchApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(FeedBatchApplication.class, args)));
    }

    @Bean
    CommandLineRunner launch(JobLauncher jobLauncher, Job gaiFeedJob, FeedProperties properties) {
        return args -> {
            // Validate required parameters before attempting to launch
            validateRequiredProperties(properties);

            log.info("Launching gaiFeedJob | feed={} | cobDate={} | ts={}",
                    properties.getFeedName(),
                    properties.getCobDate(),
                    properties.getFileTimestamp());

            JobParameters params = new JobParametersBuilder()
                    .addString("feedName",     properties.getFeedName())
                    .addString("cobDate",      properties.getCobDate())
                    .addString("fileTimestamp", properties.getFileTimestamp())
                    .addLong("runId",          System.currentTimeMillis())
                    .toJobParameters();

            try {
                JobExecution execution = jobLauncher.run(gaiFeedJob, params);

                switch (execution.getStatus()) {
                    case COMPLETED ->
                        log.info("Job completed successfully: feed={} cobDate={}",
                                properties.getFeedName(), properties.getCobDate());
                    case FAILED -> {
                        log.error("Job FAILED: feed={} cobDate={}",
                                properties.getFeedName(), properties.getCobDate());
                        execution.getAllFailureExceptions()
                                 .forEach(e -> log.error("  Cause: {}", e.getMessage(), e));
                        throw new RuntimeException("Job ended with FAILED status");
                    }
                    default ->
                        log.warn("Job ended with unexpected status: {}", execution.getStatus());
                }

            } catch (JobExecutionAlreadyRunningException e) {
                throw new IllegalStateException(
                    "Job is already running for feed='" + properties.getFeedName()
                    + "' cobDate='" + properties.getCobDate() + "'. "
                    + "Wait for it to finish or check the Batch metadata tables.", e);

            } catch (JobInstanceAlreadyCompleteException e) {
                // This run was already completed successfully — not an error, but worth noting
                log.warn("Job already completed for feed='{}' cobDate='{}'. "
                       + "Increment runId or change parameters to force a re-run.",
                        properties.getFeedName(), properties.getCobDate());

            } catch (JobRestartException e) {
                throw new IllegalStateException(
                    "Job restart failed for feed='" + properties.getFeedName() + "': "
                    + e.getMessage(), e);

            } catch (JobParametersInvalidException e) {
                throw new IllegalArgumentException(
                    "Invalid job parameters: " + e.getMessage(), e);
            }
        };
    }

    private void validateRequiredProperties(FeedProperties p) {
        if (p.getFeedName() == null || p.getFeedName().isBlank()) {
            throw new IllegalArgumentException(
                "gai.feed-name is required. "
                + "Provide --gai.feed-name=<name> or set GAI_FEED_NAME.");
        }
        if (p.getCobDate() == null || p.getCobDate().isBlank()) {
            throw new IllegalArgumentException(
                "gai.cob-date is required. "
                + "Provide --gai.cob-date=YYYYMMDD or set GAI_COB_DATE.");
        }
        if (p.getFileTimestamp() == null || p.getFileTimestamp().isBlank()) {
            throw new IllegalArgumentException(
                "gai.file-timestamp is required. "
                + "Provide --gai.file-timestamp=YYYYMMDDHHmmss or set GAI_FILE_TIMESTAMP.");
        }
    }
}
