package com.sdm.springbatch;

import com.sdm.springbatch.config.ImportFileConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ImportFileConfig.class})
@ActiveProfiles("dev")
public class JobTest {

    @Autowired
    Job job;

    @Autowired
    JobLauncher launcher;

    @Autowired
    JobRepository jobRepository;

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(job);
        utils.setJobLauncher(launcher);
        utils.setJobRepository(jobRepository);
        return utils;
    }

    @Test
    public void testJob() throws Exception {
        Map<String, JobParameter> map = new HashMap<>();
        map.put("filePath", new JobParameter("rooms.csv"));
        map.put("executedTime", new JobParameter("201812271000"));
        JobParameters parameters = new JobParameters(map);
        BatchStatus status = jobLauncherTestUtils().launchJob(parameters).getStatus();
        assertThat(status.name(), is("COMPLETED"));
    }

}
