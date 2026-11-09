package com.firstone.greenjangteo.application.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDate;

@ApiIgnore
@RestController
@RequestMapping("/cron/coupons")
@RequiredArgsConstructor
public class CouponCronController {
    private static final Logger log = LoggerFactory.getLogger(CouponCronController.class);

    private static final String ISSUE_COUPON_JOB = "issue-coupon";
    private static final String DELETE_EXPIRED_COUPON_JOB = "delete-expired-coupon";
    private static final String DUPLICATE_DELIVERY_SKIPPED = "Skipped duplicate cron delivery: {} on {}";

    private final CronAuthenticator cronAuthenticator;
    private final CronRunGuard cronRunGuard;
    private final JobScheduler jobScheduler;

    @GetMapping("/issue")
    public ResponseEntity<Void> issueCoupons(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) throws JobExecutionException {
        cronAuthenticator.authenticate(authorization);
        runOncePerDay(ISSUE_COUPON_JOB, jobScheduler::runIssueCouponJob);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/delete-expired")
    public ResponseEntity<Void> deleteExpiredCoupons(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) throws JobExecutionException {
        cronAuthenticator.authenticate(authorization);
        runOncePerDay(DELETE_EXPIRED_COUPON_JOB, jobScheduler::runDeleteExpiredCouponJob);

        return ResponseEntity.ok().build();
    }

    private void runOncePerDay(String jobName, CronJob cronJob) throws JobExecutionException {
        LocalDate today = LocalDate.now();

        if (!cronRunGuard.tryAcquire(jobName, today)) {
            log.info(DUPLICATE_DELIVERY_SKIPPED, jobName, today);
            return;
        }

        try {
            cronJob.run();
        } catch (JobExecutionException | RuntimeException e) {
            cronRunGuard.release(jobName, today);
            throw e;
        }
    }

    @FunctionalInterface
    private interface CronJob {
        void run() throws JobExecutionException;
    }
}
