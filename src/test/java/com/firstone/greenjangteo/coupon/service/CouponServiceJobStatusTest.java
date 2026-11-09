package com.firstone.greenjangteo.coupon.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceJobStatusTest {
    @InjectMocks
    private CouponServiceImpl couponService;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    @DisplayName("배치가 완료되면 예외가 발생하지 않는다.")
    @Test
    void completedJob() throws Exception {
        // given
        givenJobEndsWith(BatchStatus.COMPLETED);

        // when, then
        assertThatCode(() -> couponService.deleteExpiredCoupons()).doesNotThrowAnyException();
    }

    @DisplayName("배치가 완료되지 않으면 JobExecutionException이 발생한다.")
    @ParameterizedTest
    @EnumSource(value = BatchStatus.class, names = {"FAILED", "STOPPED", "ABANDONED", "UNKNOWN"})
    void notCompletedJob(BatchStatus batchStatus) throws Exception {
        // given
        givenJobEndsWith(batchStatus);

        // when, then
        assertThatThrownBy(() -> couponService.issueCoupons()).isInstanceOf(JobExecutionException.class);
    }

    private void givenJobEndsWith(BatchStatus batchStatus) throws Exception {
        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setStatus(batchStatus);

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
    }
}
