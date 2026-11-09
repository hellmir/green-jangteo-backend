package com.firstone.greenjangteo.application.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "cron.secret=" + CouponCronControllerTest.CRON_SECRET)
class CouponCronControllerTest {
    static final String CRON_SECRET = "test-cron-secret-0123456789";

    private static final String ISSUE_PATH = "/cron/coupons/issue";
    private static final String DELETE_EXPIRED_PATH = "/cron/coupons/delete-expired";
    private static final String VALID_AUTHORIZATION = "Bearer " + CRON_SECRET;
    private static final String ISSUE_JOB = "issue-coupon";
    private static final String DELETE_EXPIRED_JOB = "delete-expired-coupon";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobScheduler jobScheduler;

    @MockBean
    private CronRunGuard cronRunGuard;

    @DisplayName("올바른 시크릿으로 호출하면 쿠폰 발급 잡을 실행하고 200을 반환한다.")
    @Test
    void issueCoupons() throws Exception {
        // given
        when(cronRunGuard.tryAcquire(eq(ISSUE_JOB), eq(LocalDate.now()))).thenReturn(true);

        // when, then
        mockMvc.perform(get(ISSUE_PATH).header(HttpHeaders.AUTHORIZATION, VALID_AUTHORIZATION))
                .andExpect(status().isOk());

        verify(jobScheduler).runIssueCouponJob();
    }

    @DisplayName("올바른 시크릿으로 호출하면 만료 쿠폰 삭제 잡을 실행하고 200을 반환한다.")
    @Test
    void deleteExpiredCoupons() throws Exception {
        // given
        when(cronRunGuard.tryAcquire(eq(DELETE_EXPIRED_JOB), eq(LocalDate.now()))).thenReturn(true);

        // when, then
        mockMvc.perform(get(DELETE_EXPIRED_PATH).header(HttpHeaders.AUTHORIZATION, VALID_AUTHORIZATION))
                .andExpect(status().isOk());

        verify(jobScheduler).runDeleteExpiredCouponJob();
    }

    @DisplayName("Authorization 헤더가 없으면 401을 반환하고 잡을 실행하지 않는다.")
    @Test
    void rejectWithoutAuthorization() throws Exception {
        // when, then
        mockMvc.perform(get(ISSUE_PATH))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jobScheduler, cronRunGuard);
    }

    @DisplayName("틀린 시크릿은 JWT 파싱 오류 없이 401을 반환하고 잡을 실행하지 않는다.")
    @Test
    void rejectWithWrongSecret() throws Exception {
        // when, then
        mockMvc.perform(get(DELETE_EXPIRED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer wrong-secret"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jobScheduler, cronRunGuard);
    }

    @DisplayName("같은 날 이미 실행된 잡은 다시 실행하지 않고 200을 반환한다.")
    @Test
    void skipDuplicateDelivery() throws Exception {
        // given
        when(cronRunGuard.tryAcquire(eq(ISSUE_JOB), eq(LocalDate.now()))).thenReturn(false);

        // when, then
        mockMvc.perform(get(ISSUE_PATH).header(HttpHeaders.AUTHORIZATION, VALID_AUTHORIZATION))
                .andExpect(status().isOk());

        verify(jobScheduler, never()).runIssueCouponJob();
    }

    @DisplayName("잡 실행에 실패하면 실행 기록을 지우고 500을 반환한다.")
    @Test
    void releaseRunRecordWhenJobFails() throws Exception {
        // given
        LocalDate today = LocalDate.now();
        when(cronRunGuard.tryAcquire(eq(ISSUE_JOB), eq(today))).thenReturn(true);
        doThrow(new JobExecutionException("batch failed")).when(jobScheduler).runIssueCouponJob();

        // when, then
        mockMvc.perform(get(ISSUE_PATH).header(HttpHeaders.AUTHORIZATION, VALID_AUTHORIZATION))
                .andExpect(status().isInternalServerError());

        verify(cronRunGuard).release(ISSUE_JOB, today);
    }

    @DisplayName("GET 외의 메서드는 잡을 실행하지 않는다.")
    @Test
    void rejectNonGetMethod() throws Exception {
        // when, then
        mockMvc.perform(post(ISSUE_PATH).header(HttpHeaders.AUTHORIZATION, VALID_AUTHORIZATION))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(200));

        verifyNoInteractions(jobScheduler, cronRunGuard);
    }
}
