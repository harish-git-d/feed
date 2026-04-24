package com.ccc.risk.credit.gai.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Step listener for the SFTP transfer step.
 *
 * <p>Logs a clear retry hint on failure so the on-call engineer knows
 * exactly what command to run to re-attempt the transfer without
 * re-generating the data files.
 */
@Slf4j
@Component
public class SftpRetryStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting SFTP transfer step...");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus().isUnsuccessful()) {
            String feedName = stepExecution.getJobExecution()
                    .getJobParameters().getString("feedName", "unknown");
            String cobDate  = stepExecution.getJobExecution()
                    .getJobParameters().getString("cobDate", "unknown");

            log.error("SFTP transfer step FAILED for feed='{}' cobDate='{}'.", feedName, cobDate);
            log.error("Data files are still in the output directory. "
                    + "Fix the SFTP issue and re-run the job with the same parameters — "
                    + "Spring Batch will restart from the failed transferFilesStep.");
        }
        return stepExecution.getExitStatus();
    }
}
