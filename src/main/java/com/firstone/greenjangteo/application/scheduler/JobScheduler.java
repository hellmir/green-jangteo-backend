package com.firstone.greenjangteo.application.scheduler;

import com.firstone.greenjangteo.coupon.dto.request.IssueCouponsRequestDto;
import com.firstone.greenjangteo.coupon.dto.request.ProvideCouponsToUsersRequestDto;
import com.firstone.greenjangteo.coupon.model.CouponAndGroupEntityToDtoMapper;
import com.firstone.greenjangteo.coupon.model.entity.CouponGroup;
import com.firstone.greenjangteo.coupon.repository.CouponGroupRepository;
import com.firstone.greenjangteo.coupon.service.CouponService;
import com.firstone.greenjangteo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobScheduler {
    private final CouponService couponService;
    private final CouponGroupRepository couponGroupRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private static final String SCHEDULED_ISSUE_JOB_START = "Beginning to scheduled issuing coupon job";

    private static final String SCHEDULED_DELETE_EXPIRED_JOB_START
            = "Beginning to scheduled deleting expired coupon job";

    private int userSize;
    private boolean isIssueToAllUsersRequired;

    /**
     * 매일 00시(UTC) Vercel Cron이 호출 (vercel.json)
     */
    public void runIssueCouponJob() throws JobExecutionException {
        log.info(SCHEDULED_ISSUE_JOB_START);

        LocalDate today = LocalDate.now();
        List<CouponGroup> scheduledCouponGroups = couponGroupRepository.findByScheduledIssueDate(today);

        if (scheduledCouponGroups.isEmpty()) {
            return;
        }

        userSize = (int) userRepository.count();

        for (CouponGroup couponGroup : scheduledCouponGroups) {
            createRequiredCoupons(couponGroup);
        }

        couponService.issueCoupons();

        if (isIssueToAllUsersRequired) {
            List<Long> userIds = userRepository.findAllUserIds();
            for (CouponGroup couponGroup : scheduledCouponGroups) {
                provideCouponsToUsers(couponGroup, userIds);
            }
        }

        isIssueToAllUsersRequired = false;
    }

    /**
     * 매일 06시(UTC) Vercel Cron이 호출 (vercel.json)
     */
    public void runDeleteExpiredCouponJob() throws JobExecutionException {
        log.info(SCHEDULED_DELETE_EXPIRED_JOB_START);
        couponService.deleteExpiredCoupons();
    }

    private void createRequiredCoupons(CouponGroup couponGroup) throws JobExecutionException {
        if (couponGroup.isIssueToAllUsersRequired()) {
            isIssueToAllUsersRequired = true;
            IssueCouponsRequestDto issueCouponsRequestDto
                    = CouponAndGroupEntityToDtoMapper.toIssueCouponsRequestDto(couponGroup, userSize);

            couponService.createCoupons(issueCouponsRequestDto);
        }
    }

    private void provideCouponsToUsers(CouponGroup couponGroup, List<Long> userIds) throws JobExecutionException {
        if (couponGroup.isIssueToAllUsersRequired()) {
            ProvideCouponsToUsersRequestDto provideCouponsToUsersRequestDto
                    = new ProvideCouponsToUsersRequestDto(couponGroup.getId(), userIds, userSize);
            couponService.provideCouponsToUsers(provideCouponsToUsersRequestDto);
        }
    }
}


