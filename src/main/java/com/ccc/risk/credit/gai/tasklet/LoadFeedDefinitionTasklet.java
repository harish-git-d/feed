package com.ccc.risk.credit.gai.tasklet;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.ccc.risk.credit.gai.service.FeedDefinitionLoader;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class LoadFeedDefinitionTasklet implements Tasklet {

    private final FeedDefinitionLoader loader;
    private final FeedProperties properties;

    public LoadFeedDefinitionTasklet(FeedDefinitionLoader loader, FeedProperties properties) {
        this.loader = loader;
        this.properties = properties;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        FeedDefinition definition = loader.load(properties.getFeedName());
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
                .put("feedDefinition", definition);
        return RepeatStatus.FINISHED;
    }
}
