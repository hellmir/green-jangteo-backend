package com.firstone.greenjangteo.application.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CronRunGuardTest {
    private static final String JOB_NAME = "issue-coupon";
    private static final LocalDate RUN_DATE = LocalDate.of(2026, 11, 9);
    private static final String EXPECTED_KEY = "green-jangteo:cron:issue-coupon:2026-11-09";
    private static final Duration EXPECTED_TTL = Duration.ofDays(2);

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CronRunGuard cronRunGuard;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        cronRunGuard = new CronRunGuard(stringRedisTemplate);
    }

    @DisplayName("그날 첫 실행이면 실행 기록을 남기고 true를 반환한다.")
    @Test
    void acquireFirstRun() {
        // given
        when(valueOperations.setIfAbsent(EXPECTED_KEY, "1", EXPECTED_TTL)).thenReturn(true);

        // when, then
        assertThat(cronRunGuard.tryAcquire(JOB_NAME, RUN_DATE)).isTrue();
    }

    @DisplayName("이미 실행 기록이 있으면 false를 반환한다.")
    @Test
    void rejectDuplicateRun() {
        // given
        when(valueOperations.setIfAbsent(EXPECTED_KEY, "1", EXPECTED_TTL)).thenReturn(false);

        // when, then
        assertThat(cronRunGuard.tryAcquire(JOB_NAME, RUN_DATE)).isFalse();
    }

    @DisplayName("Redis 응답이 없으면 실행하지 않도록 false를 반환한다.")
    @Test
    void rejectWhenRedisRespondsNull() {
        // given
        when(valueOperations.setIfAbsent(EXPECTED_KEY, "1", EXPECTED_TTL)).thenReturn(null);

        // when, then
        assertThat(cronRunGuard.tryAcquire(JOB_NAME, RUN_DATE)).isFalse();
    }

    @DisplayName("Redis 장애 시에는 그날 잡을 놓치지 않도록 실행을 허용한다.")
    @Test
    void acquireWhenRedisIsUnavailable() {
        // given
        when(valueOperations.setIfAbsent(EXPECTED_KEY, "1", EXPECTED_TTL))
                .thenThrow(new RedisConnectionFailureException("Redis is unavailable"));

        // when, then
        assertThat(cronRunGuard.tryAcquire(JOB_NAME, RUN_DATE)).isTrue();
    }

    @DisplayName("실행 기록을 지울 수 있다.")
    @Test
    void release() {
        // when
        cronRunGuard.release(JOB_NAME, RUN_DATE);

        // then
        verify(stringRedisTemplate).delete(EXPECTED_KEY);
    }

    @DisplayName("Redis 장애로 실행 기록을 지우지 못해도 원래의 잡 실패를 가리지 않는다.")
    @Test
    void releaseWhenRedisIsUnavailable() {
        // given
        when(stringRedisTemplate.delete(EXPECTED_KEY))
                .thenThrow(new RedisConnectionFailureException("Redis is unavailable"));

        // when, then
        assertThatCode(() -> cronRunGuard.release(JOB_NAME, RUN_DATE)).doesNotThrowAnyException();
    }
}
