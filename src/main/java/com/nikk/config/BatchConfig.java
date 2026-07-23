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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Autowired
    private ICustomerRepo repo;

    @Autowired
    private JobRepository jobRepo;

    // Item Reader
    @Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> reader= new FlatFileItemReaderBuilder<Customer>()
                .name("customerCsvReader")
                .resource(new ClassPathResource("customers_1000.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper())
                .build();
        return reader;

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
        ChunkOrientedStepBuilder<Customer, Customer> stepBuilder =
                new StepBuilder("step-1", jobRepo).<Customer, Customer>chunk(10);

        return stepBuilder
                .transactionManager(transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job job(Step step) {
        return new JobBuilder("customer-import", jobRepo)
                .start(step)
                .build();
    }
}
