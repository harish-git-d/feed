package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FileMetadata;
import com.ccc.risk.credit.gai.service.SftpTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * SFTP-transfers all generated feed files (EVENT, RECORD, ATTRIBUTE, CONTROL)
 * to the GAI target.
 *
 * <p>Pre-flight: verifies each file exists locally before initiating any
 * transfer. Fails fast with a clear message listing any missing files,
 * rather than failing mid-transfer after some files have already landed.
 *
 * <p>The control file must be last in {@code deliveredFiles} — this is
 * guaranteed by {@link GenerateControlFileTasklet} appending it at the end.
 * GAI uses the control file as the processing trigger.
 *
 * <p>On SFTP failure the {@link SftpTransferService} retries up to 3 times
 * with exponential backoff. If all retries are exhausted, the exception
 * propagates to Spring Batch which marks the step FAILED. The data files
 * remain on disk — re-running the job restarts from this step only.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class TransferFilesTasklet implements Tasklet {

    private final FeedProperties      properties;
    private final SftpTransferService transferService;

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        ExecutionContext jobCtx = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

        List<FileMetadata> deliveredFiles = (List<FileMetadata>) jobCtx.get("deliveredFiles");
        if (deliveredFiles == null || deliveredFiles.isEmpty()) {
            throw new IllegalStateException(
                "No files found in deliveredFiles context. "
                + "Cannot proceed with SFTP transfer — check preceding steps.");
        }

        // --- Pre-flight: verify all files exist before touching SFTP ---
        List<String> missing = new ArrayList<>();
        List<Path>   paths   = new ArrayList<>();

        for (FileMetadata meta : deliveredFiles) {
            Path localPath = Paths.get(properties.getOutputDirectory(), meta.getFileName());
            if (!Files.exists(localPath)) {
                missing.add(meta.getFileName());
            } else {
                paths.add(localPath);
            }
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                "Pre-flight check failed — the following files are missing from '"
                + properties.getOutputDirectory() + "' and cannot be transferred:\n  "
                + String.join("\n  ", missing));
        }

        // --- Transfer (SftpTransferService handles retries internally) ---
        log.info("Pre-flight OK. Transferring {} file(s) via SFTP...", paths.size());
        try {
            transferService.transfer(paths);
        } catch (IllegalStateException e) {
            // Add feed/cobDate context to the error for easier triage
            String feedName = properties.getFeedName();
            String cobDate  = properties.getCobDate();
            throw new IllegalStateException(
                String.format("[%s][%s] SFTP transfer failed after all retries: %s",
                        feedName, cobDate, e.getMessage()), e.getCause());
        }

        return RepeatStatus.FINISHED;
    }
}
