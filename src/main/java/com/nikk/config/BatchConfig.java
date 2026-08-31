package com.nikk.config;

import com.nikk.entity.Customer;
import com.nikk.repo.ICustomerRepo;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final ICustomerRepo repo;
    private final JobRepository jobRepository;
    private final Resource inputResource;
    private final int chunkSize;
    private final long skipLimit;
    private final long retryLimit;

    public BatchConfig(ICustomerRepo repo,
                       JobRepository jobRepository,
                       @Value("${etl.input-resource}") Resource inputResource,
                       @Value("${etl.chunk-size}") int chunkSize,
                       @Value("${etl.skip-limit}") long skipLimit,
                       @Value("${etl.retry-limit}") long retryLimit) {
        this.repo = repo;
        this.jobRepository = jobRepository;
        this.inputResource = inputResource;
        this.chunkSize = chunkSize;
        this.skipLimit = skipLimit;
        this.retryLimit = retryLimit;
    }

    // Item Reader
    @Bean
    public FlatFileItemReader<Customer> reader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerCsvReader")
                .resource(inputResource)
                .linesToSkip(1)
                .lineMapper(lineMapper())
                .build();

    }

    @Bean
    public LineMapper<Customer> lineMapper() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("customerId", "firstname", "lastname", "email", "city", "state", "country", "zipcode");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }


    // Item Processor
    @Bean
    public CustomerProcessor processCxData() {
        return new CustomerProcessor();
    }

    // Item Writer
    @Bean
    public RepositoryItemWriter<Customer> itemWriter() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>(repo);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step(FlatFileItemReader<Customer> reader,
                     CustomerProcessor processor,
                     RepositoryItemWriter<Customer> writer,
                     PlatformTransactionManager transactionManager) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("etl.chunk-size must be greater than zero");
        }

        ChunkOrientedStepBuilder<Customer, Customer> stepBuilder =
                new StepBuilder("customer-import-step", jobRepository)
                        .<Customer, Customer>chunk(chunkSize);

        return stepBuilder
                .transactionManager(transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(CustomerValidationException.class, DataIntegrityViolationException.class)
                .skipLimit(skipLimit)
                .retry(CannotAcquireLockException.class)
                .retryLimit(retryLimit)
                .build();
    }

    @Bean
    public Job job(Step step) {
        return new JobBuilder("customer-import", jobRepository)
                .start(step)
                .build();
    }
}
