package com.firstone.greenjangteo.application.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Vercel Cron은 같은 실행을 드물게 두 번 전달할 수 있으므로 잡별로 하루 1회만 실행되도록 막는다.
 */
@Component
@RequiredArgsConstructor
public class CronRunGuard {
    private static final Logger log = LoggerFactory.getLogger(CronRunGuard.class);

    private static final String KEY_FORMAT = "green-jangteo:cron:%s:%s";
    private static final String GUARD_UNAVAILABLE = "Cron run guard unavailable: {} on {}";
    private static final String RUN_RECORD = "1";
    private static final Duration RUN_RECORD_TTL = Duration.ofDays(2);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquire(String jobName, LocalDate runDate) {
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(generateKey(jobName, runDate), RUN_RECORD, RUN_RECORD_TTL);

            return Boolean.TRUE.equals(acquired);
        } catch (DataAccessException e) {
            // Vercel Cron은 재시도하지 않으므로 중복 위험보다 그날 잡을 놓치는 쪽을 막는다
            log.warn(GUARD_UNAVAILABLE, jobName, runDate, e);
            return true;
        }
    }

    public void release(String jobName, LocalDate runDate) {
        try {
            stringRedisTemplate.delete(generateKey(jobName, runDate));
        } catch (DataAccessException e) {
            log.warn(GUARD_UNAVAILABLE, jobName, runDate, e);
        }
    }

    private String generateKey(String jobName, LocalDate runDate) {
        return String.format(KEY_FORMAT, jobName, runDate);
    }
}
