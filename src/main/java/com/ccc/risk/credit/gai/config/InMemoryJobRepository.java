package com.ccc.risk.credit.gai.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fully in-memory {@link JobRepository} implementation.
 *
 * <p>Stores all Spring Batch metadata (job instances, job executions,
 * step executions) in {@link ConcurrentHashMap}s. No database tables
 * are created or required — not even H2.
 *
 * <p>Data is ephemeral: it exists only for the duration of the JVM process,
 * which is fine for this application since each run is independent.
 *
 * <p>Thread-safe via atomic ID generation and concurrent maps.
 */
public class InMemoryJobRepository implements JobRepository {

    private final Map<Long, JobInstance>  jobInstances  = new ConcurrentHashMap<>();
    private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();
    private final Map<Long, StepExecution> stepExecutions = new ConcurrentHashMap<>();

    private final AtomicLong instanceIdSeq  = new AtomicLong(1);
    private final AtomicLong executionIdSeq = new AtomicLong(1);
    private final AtomicLong stepIdSeq      = new AtomicLong(1);

    // -------------------------------------------------------------------------
    // JobInstance
    // -------------------------------------------------------------------------

    @Override
    public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {
        long id = instanceIdSeq.getAndIncrement();
        JobInstance instance = new JobInstance(id, jobName);
        instance.incrementVersion();
        jobInstances.put(id, instance);
        return instance;
    }

    public boolean isJobInstanceExists(String jobName, JobParameters jobParameters) {
        return jobInstances.values().stream()
                .anyMatch(i -> i.getJobName().equals(jobName));
    }

    public JobInstance getJobInstance(String jobName, JobParameters jobParameters) {
        return jobInstances.values().stream()
                .filter(i -> i.getJobName().equals(jobName))
                .findFirst()
                .orElse(null);
    }

    public List<JobInstance> getJobInstances(String jobName, int start, int count) {
        return jobInstances.values().stream()
                .filter(i -> i.getJobName().equals(jobName))
                .skip(start)
                .limit(count)
                .toList();
    }

    public List<JobInstance> findJobInstancesByName(String jobName, int start, int count) {
        return getJobInstances(jobName, start, count);
    }

    public long getJobInstanceCount(String jobName) {
        return jobInstances.values().stream()
                .filter(i -> i.getJobName().equals(jobName))
                .count();
    }

    // -------------------------------------------------------------------------
    // JobExecution
    // -------------------------------------------------------------------------

    @Override
    public JobExecution createJobExecution(String jobName, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException,
                   JobRestartException,
                   JobInstanceAlreadyCompleteException {

        JobInstance instance = getJobInstance(jobName, jobParameters);
        if (instance == null) {
            instance = createJobInstance(jobName, jobParameters);
        }
        return createJobExecution(instance, jobParameters, null);
    }

    public JobExecution createJobExecution(JobInstance jobInstance,
                                           JobParameters jobParameters,
                                           String jobConfigurationLocation) {
        long id = executionIdSeq.getAndIncrement();
        JobExecution execution = new JobExecution(jobInstance, id, jobParameters);
        execution.setExecutionContext(new ExecutionContext());
        execution.setStatus(BatchStatus.STARTING);
        execution.setCreateTime(LocalDateTime.now());
        jobExecutions.put(id, execution);
        return execution;
    }

    @Override
    public void update(JobExecution jobExecution) {
        jobExecution.setLastUpdated(LocalDateTime.now());
        jobExecutions.put(jobExecution.getId(), jobExecution);
    }

    @Override
    public void updateExecutionContext(JobExecution jobExecution) {
        jobExecutions.put(jobExecution.getId(), jobExecution);
    }

    @Override
    public JobExecution getLastJobExecution(String jobName, JobParameters jobParameters) {
        return jobExecutions.values().stream()
                .filter(e -> e.getJobInstance().getJobName().equals(jobName))
                .max(Comparator.comparing(JobExecution::getId))
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // StepExecution
    // -------------------------------------------------------------------------

    @Override
    public void add(StepExecution stepExecution) {
        long id = stepIdSeq.getAndIncrement();
        stepExecution.setId(id);
        stepExecution.setVersion(0);
        stepExecution.setLastUpdated(LocalDateTime.now());
        stepExecutions.put(id, stepExecution);
    }

    @Override
    public void addAll(Collection<StepExecution> stepExecutions) {
        stepExecutions.forEach(this::add);
    }

    @Override
    public void update(StepExecution stepExecution) {
        stepExecution.setLastUpdated(LocalDateTime.now());
        stepExecutions.put(stepExecution.getId(), stepExecution);
    }

    @Override
    public void updateExecutionContext(StepExecution stepExecution) {
        stepExecutions.put(stepExecution.getId(), stepExecution);
    }

    @Override
    public StepExecution getLastStepExecution(JobInstance jobInstance, String stepName) {
        return stepExecutions.values().stream()
                .filter(s -> s.getJobExecution().getJobInstance().getId()
                              .equals(jobInstance.getId())
                          && s.getStepName().equals(stepName))
                .max(Comparator.comparing(StepExecution::getId))
                .orElse(null);
    }

    @Override
    public long getStepExecutionCount(JobInstance jobInstance, String stepName) {
        return stepExecutions.values().stream()
                .filter(s -> s.getJobExecution().getJobInstance().getId()
                              .equals(jobInstance.getId())
                          && s.getStepName().equals(stepName))
                .count();
    }

    // -------------------------------------------------------------------------
    // Delete (no-op — in-memory GC handles cleanup)
    // -------------------------------------------------------------------------

    @Override
    public void deleteStepExecution(StepExecution stepExecution) {
        stepExecutions.remove(stepExecution.getId());
    }

    @Override
    public void deleteJobExecution(JobExecution jobExecution) {
        jobExecutions.remove(jobExecution.getId());
    }

    public void deleteJobInstanceAndRelatedData(JobInstance jobInstance) {
        jobInstances.remove(jobInstance.getId());
        jobExecutions.values().removeIf(
                e -> e.getJobInstance().getId().equals(jobInstance.getId()));
    }
}
