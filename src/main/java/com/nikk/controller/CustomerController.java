package com.nikk.controller;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
public class CustomerController {

    private final JobOperator jobOperator;
    private final Job job;
    private final JobRepository jobRepository;

    public CustomerController(JobOperator jobOperator, Job job, JobRepository jobRepository) {
        this.jobOperator = jobOperator;
        this.job = job;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/api/v1/imports")
    public ImportResponse startImport() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobOperator.start(job, jobParameters);
        return toResponse(execution);
    }

    @GetMapping("/api/v1/imports/{executionId}")
    public ImportResponse getImport(@PathVariable long executionId) {
        JobExecution execution = jobRepository.getJobExecution(executionId);
        if (execution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Import execution not found");
        }
        return toResponse(execution);
    }

    private ImportResponse toResponse(JobExecution execution) {
        long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
        long written = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        long skipped = execution.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();

        return new ImportResponse(execution.getId(), execution.getStatus().name(), read, written, skipped,
                execution.getStartTime(), execution.getEndTime());
    }

    public record ImportResponse(
            long executionId,
            String status,
            long recordsRead,
            long recordsWritten,
            long recordsSkipped,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
    }
}
