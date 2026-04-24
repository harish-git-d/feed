package com.ccc.risk.credit.gai.listener;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Job-level listener that handles two concerns:
 *
 * <p><b>On failure:</b>
 * <ul>
 *   <li>Deletes any partially-written .dat.gz files from the output directory
 *       so that a re-run starts clean and doesn't send corrupt files via SFTP.</li>
 *   <li>Sends an email alert if {@code gai.notification.enabled=true}.</li>
 * </ul>
 *
 * <p><b>On success:</b> logs a completion summary.
 */
@Slf4j
@Component
public class FeedJobExecutionListener implements JobExecutionListener {

    private final FeedProperties feedProperties;
    private final JavaMailSender mailSender;

    public FeedJobExecutionListener(FeedProperties feedProperties,
                                    @Autowired(required = false) JavaMailSender mailSender) {
        this.feedProperties = feedProperties;
        this.mailSender = mailSender;
    }

    // -------------------------------------------------------------------------
    // JobExecutionListener
    // -------------------------------------------------------------------------

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== GAI Feed Job STARTING | feed={} | cobDate={} | ts={} ===",
                feedProperties.getFeedName(),
                feedProperties.getCobDate(),
                feedProperties.getFileTimestamp());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterJob(JobExecution jobExecution) {
        String feedName = feedProperties.getFeedName();
        String cobDate  = feedProperties.getCobDate();
        BatchStatus status = jobExecution.getStatus();

        if (status == BatchStatus.COMPLETED) {
            List<FileMetadata> delivered =
                    (List<FileMetadata>) jobExecution.getExecutionContext().get("deliveredFiles");
            int fileCount = delivered != null ? delivered.size() : 0;
            log.info("=== GAI Feed Job COMPLETED | feed={} | cobDate={} | files={} ===",
                    feedName, cobDate, fileCount);
            return;
        }

        if (status == BatchStatus.FAILED) {
            log.error("=== GAI Feed Job FAILED | feed={} | cobDate={} ===", feedName, cobDate);

            // Log all failure exceptions with full stack traces
            jobExecution.getAllFailureExceptions().forEach(ex ->
                    log.error("Failure cause: {}", ex.getMessage(), ex));

            // Clean up partial output files so a re-run starts fresh
            cleanupPartialFiles(jobExecution);

            // Send email alert if configured
            if (feedProperties.getNotification().isEnabled()) {
                sendFailureAlert(jobExecution);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Partial file cleanup
    // -------------------------------------------------------------------------

    /**
     * Deletes any .dat or .dat.gz files in the output directory that were
     * written during this failed run, identified by the feed name and timestamp.
     * This prevents stale/incomplete files from being picked up on a retry.
     */
    @SuppressWarnings("unchecked")
    private void cleanupPartialFiles(JobExecution jobExecution) {
        List<FileMetadata> deliveredFiles =
                (List<FileMetadata>) jobExecution.getExecutionContext().get("deliveredFiles");

        if (deliveredFiles == null || deliveredFiles.isEmpty()) {
            log.info("No partial files to clean up.");
            return;
        }

        log.warn("Cleaning up {} partial file(s) from failed run...", deliveredFiles.size());

        for (FileMetadata meta : deliveredFiles) {
            Path path = Paths.get(feedProperties.getOutputDirectory(), meta.getFileName());
            try {
                boolean deleted = Files.deleteIfExists(path);
                if (deleted) {
                    log.warn("Deleted partial file: {}", meta.getFileName());
                }
            } catch (Exception e) {
                // Non-fatal — log and continue cleaning up remaining files
                log.error("Failed to delete partial file {}: {}", meta.getFileName(), e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Email alert
    // -------------------------------------------------------------------------

    private void sendFailureAlert(JobExecution jobExecution) {
        String feedName = feedProperties.getFeedName();
        String cobDate  = feedProperties.getCobDate();

        StringBuilder failureDetails = new StringBuilder();
        jobExecution.getAllFailureExceptions().forEach(ex -> {
            failureDetails.append("  ").append(ex.getClass().getSimpleName())
                          .append(": ").append(ex.getMessage()).append("\n");
            if (ex.getCause() != null) {
                failureDetails.append("    Caused by: ")
                              .append(ex.getCause().getMessage()).append("\n");
            }
        });

        String subject = String.format("[GAI Feed] FAILED — %s | COB: %s", feedName, cobDate);
        String body = String.format(
                "Feed Job:     %s%n"
                + "COB Date:     %s%n"
                + "Status:       FAILED%n"
                + "Start Time:   %s%n"
                + "End Time:     %s%n"
                + "%nFailure Details:%n%s%n"
                + "%nPartial output files have been deleted. "
                + "Re-run the job once the issue is resolved.%n"
                + "%nOutput Dir:   %s",
                feedName,
                cobDate,
                jobExecution.getStartTime() != null
                        ? jobExecution.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "N/A",
                jobExecution.getEndTime() != null
                        ? jobExecution.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "N/A",
                failureDetails,
                feedProperties.getOutputDirectory()
        );

        try {
            FeedProperties.Notification n = feedProperties.getNotification();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(n.getFrom());
            message.setTo(n.getTo());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Failure alert sent to {}", n.getTo());
        } catch (Exception e) {
            // Email failure must not mask the original job failure
            log.error("Failed to send failure alert email: {}", e.getMessage());
        }
    }
}
