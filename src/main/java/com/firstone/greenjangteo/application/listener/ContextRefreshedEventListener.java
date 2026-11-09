package com.firstone.greenjangteo.application.listener;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Date;
import java.util.Set;

@Profile("!vercel") // 인스턴스가 동시에 뜨는 환경에서는 다른 인스턴스의 실행 중인 잡까지 중단 처리하게 된다
@Configuration
@RequiredArgsConstructor
public class ContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;

    private static final Logger log = LoggerFactory.getLogger(ContextRefreshedEventListener.class);

    private static final String STOPPING_JOB_START = "Beginning to stop running jobs.";
    private static final String UPDATE_EXECUTION_STATUS = "Updated job execution status by jobId: {}";
    private static final String STOPPING_JOB_COMPLETE = "Stopped running jobs.";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info(STOPPING_JOB_START);
        for (String jobName : jobExplorer.getJobNames()) {
            Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(jobName);

            for (JobExecution jobExecution : runningJobExecutions) {
                jobExecution.setStatus(BatchStatus.STOPPED);
                jobExecution.setEndTime(new Date());

                for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                    if (stepExecution.getStatus().isRunning()) {
                        stepExecution.setStatus(BatchStatus.STOPPED);
                        stepExecution.setEndTime(new Date());
                        jobRepository.update(stepExecution);
                    }
                }
                jobRepository.update(jobExecution);
                log.info(UPDATE_EXECUTION_STATUS, jobExecution.getJobId());
            }
        }
        log.info(STOPPING_JOB_COMPLETE);
    }
}
