package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.service.SftpTransferService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class TransferFilesTasklet implements Tasklet {

    private final FeedProperties properties;
    private final SftpTransferService transferService;

    public TransferFilesTasklet(FeedProperties properties, SftpTransferService transferService) {
        this.properties = properties;
        this.transferService = transferService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<String> deliveredFiles = (List<String>) chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext().get("deliveredFiles");
        if (deliveredFiles == null) {
            deliveredFiles = new ArrayList<>();
        }
        List<Path> paths = deliveredFiles.stream()
                .map(name -> Paths.get(properties.getOutputDirectory(), name))
                .toList();
        transferService.transfer(paths);
        return RepeatStatus.FINISHED;
    }
}
