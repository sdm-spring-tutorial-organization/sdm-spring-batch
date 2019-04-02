package com.sdm.springbatch.config;

import com.sdm.springbatch.TruncateService;
import com.sdm.springbatch.TruncateServiceImpl;
import com.sdm.springbatch.model.Room;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
/*@Import({ InfrastructureConfig.class, JpaIntrastructureConfig.class })*/
public class ImportFileConfig {

    public static final String JOB_NAME = "importFileJob";

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Bean
    public JobParametersValidator jobParametersValidator() {
        String[] requiredKeys = new String[] { "filePath" };
        String[] optionalKeys = new String[] { "executedTime" };

        return new DefaultJobParametersValidator(requiredKeys, optionalKeys);
    }

    @Bean
    public Job importFileJob() throws Exception {
        return jobBuilderFactory.get(JOB_NAME)
                .validator(jobParametersValidator())
                .start(truncateStep())
                .next(importFileStep())
                .build();
    }

    @Bean
    public Step truncateStep() {
        return stepBuilderFactory.get("truncateStep")
                .tasklet(truncateTasklet()).build();
    }

    @Bean
    public MethodInvokingTaskletAdapter truncateTasklet() {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();
        adapter.setTargetObject(truncateService());
        adapter.setTargetMethod("execute");
        return adapter;
    }

    @Bean
    public TruncateService truncateService() {
        return new TruncateServiceImpl();
    }

    @Bean
    public Step importFileStep() {
        return stepBuilderFactory.get("importFileStep").<Room, Room>chunk(100)
                .reader(fileItemReader(null))
                .writer(dbItemWriter())
                .build();
    }

    @Bean
    @StepScope
    @Value("#{jobParameters['filePath']}")
    public FlatFileItemReader<Room> fileItemReader(String filePath) {
        FlatFileItemReader<Room> fileItemReader = new FlatFileItemReader<Room>();
        ResourceLoader loader = new DefaultResourceLoader();
        fileItemReader.setResource(loader.getResource(filePath));

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[] {"roomId", "roomName", "capacity"});

        DefaultLineMapper<Room> lineMapper = new DefaultLineMapper<Room>();
        lineMapper.setLineTokenizer(lineTokenizer);

        BeanWrapperFieldSetMapper<Room> fieldSetMapper = new BeanWrapperFieldSetMapper<Room>();
        fieldSetMapper.setTargetType(Room.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        fileItemReader.setLineMapper(lineMapper);
        fileItemReader.setLinesToSkip(1);
        return fileItemReader;
    }

    @Bean
    @StepScope
    public ItemWriter<Room> dbItemWriter() {
        JpaItemWriter<Room> jpaItemWriter = new JpaItemWriter<Room>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }



}
