package com.ccc.risk.credit.gai;

import com.ccc.risk.credit.gai.config.FeedProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(FeedProperties.class)
public class FeedBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedBatchApplication.class, args);
    }

    @Bean
    CommandLineRunner launch(JobLauncher jobLauncher, Job gaiFeedJob, FeedProperties properties) {
        return args -> {
            JobParameters parameters = new JobParametersBuilder()
                    .addString("feedName", properties.getFeedName())
                    .addString("cobDate", properties.getCobDate())
                    .addString("fileTimestamp", properties.getFileTimestamp())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(gaiFeedJob, parameters);
        };
    }
}
