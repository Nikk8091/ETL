package com.nikk.config;

import com.nikk.repo.ICustomerRepo;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CustomerImportJobTests {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job customerImportJob;

    @Autowired
    private ICustomerRepo customerRepository;

    @Test
    void importsCsvAndPublishesExecutionMetrics() throws Exception {
        JobExecution execution = jobOperator.start(
                customerImportJob,
                new JobParametersBuilder().addLong("testRun", System.nanoTime()).toJobParameters()
        );

        long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
        long written = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1000, read);
        assertEquals(1000, written);
        assertEquals(1000, customerRepository.count());
    }
}
