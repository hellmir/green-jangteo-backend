package com.firstone.greenjangteo.coupon.service;

import com.firstone.greenjangteo.coupon.dto.request.IssueCouponsRequestDto;
import com.firstone.greenjangteo.coupon.dto.request.ProvideCouponsToUserRequestDto;
import com.firstone.greenjangteo.coupon.dto.request.ProvideCouponsToUsersRequestDto;
import com.firstone.greenjangteo.coupon.model.entity.Coupon;
import com.firstone.greenjangteo.coupon.model.entity.CouponGroup;
import com.firstone.greenjangteo.coupon.repository.CouponRepository;
import com.firstone.greenjangteo.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.firstone.greenjangteo.coupon.exception.message.NotFoundExceptionMessage.COUPON_ID_NOT_FOUND_EXCEPTION;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final JobLauncher jobLauncher;

    private final Job createCouponJob;
    private final Job issueCouponJob;
    private final Job provideCouponJob;
    private final Job deleteExpiredCouponJob;

    private final CouponGroupService couponGroupService;
    private final CouponRepository couponRepository;

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private static final String QUANTITY_SHORTAGE_MESSAGE = "couponsRemained: {} is less than requiredQuantity: {}";
    private static final String JOB_NOT_COMPLETED_MESSAGE = "Job did not complete: %s, status: %s";

    @Override
    public void createCoupons(IssueCouponsRequestDto issueCouponsRequestDto) throws JobExecutionException {
        if (issueCouponsRequestDto.isIssueQuantityIsMinusOne()) {
            couponGroupService.addCouponGroupToImmediatelyIssue(issueCouponsRequestDto);
            return;
        }

        String scheduledIssueDate = issueCouponsRequestDto.getScheduledIssueDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("couponName", issueCouponsRequestDto.getCouponName())
                .addString("amount", issueCouponsRequestDto.getAmount())
                .addString("description", issueCouponsRequestDto.getDescription())
                .addString("issueQuantity", issueCouponsRequestDto.getIssueQuantity())
                .addString("scheduledIssueDate", scheduledIssueDate)
                .addString("expirationPeriod", issueCouponsRequestDto.getExpirationPeriod())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        runJob(createCouponJob, jobParameters);
    }

    @Override
    public void issueCoupons() throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        runJob(issueCouponJob, jobParameters);
    }

    @Override
    public void provideCouponsToUsers(ProvideCouponsToUsersRequestDto provideCouponsToUsersRequestDto)
            throws JobExecutionException {
        String userIds = parseUserIdsToStringValue(provideCouponsToUsersRequestDto.getUserIds());

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("couponGroupId", provideCouponsToUsersRequestDto.getCouponGroupId())
                .addString("userIds", userIds)
                .addLong("quantity", (long) provideCouponsToUsersRequestDto.getQuantity())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        runJob(provideCouponJob, jobParameters);
    }

    @Override
    public void deleteExpiredCoupons() throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        runJob(deleteExpiredCouponJob, jobParameters);
    }

    @Override
    public void provideCouponsToUser(ProvideCouponsToUserRequestDto provideCouponsToUserRequestDto) {
        CouponGroup couponGroup;
        try {
            couponGroup = couponGroupService.getCouponGroup(provideCouponsToUserRequestDto.getCouponName());
        } catch (EntityNotFoundException e) {
            log.warn(e.getMessage());
            return;
        }

        int requiredQuantity = provideCouponsToUserRequestDto.getQuantity();

        if (!couponGroup.isCouponsRemained(requiredQuantity)) {
            log.warn(QUANTITY_SHORTAGE_MESSAGE, couponGroup.getRemainingQuantity(), requiredQuantity);
            couponGroup.addInsufficientCoupons(requiredQuantity);
        }

        User user = provideCouponsToUserRequestDto.getUser();

        issueAndAddUserToCoupons(user, couponGroup, requiredQuantity);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 15)
    public List<Coupon> getCoupons(Long userId) {
        return couponRepository.findAllByUserId(userId);
    }

    @Override
    public int updateUsedCoupon(Long orderId, Long couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.addOrderId(orderId);
        couponRepository.save(coupon);

        return coupon.getCouponGroup().getAmount().getValue();
    }

    @Override
    public int rollBackUsedCoupon(Long orderId, Long couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.removeOrderId(orderId);
        couponRepository.save(coupon);

        return coupon.getCouponGroup().getAmount().getValue();
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, timeout = 10)
    public void deleteCoupon(long couponId) {
        if (couponRepository.existsById(couponId)) {
            couponRepository.deleteById(couponId);
            return;
        }

        throw new EntityNotFoundException(COUPON_ID_NOT_FOUND_EXCEPTION + couponId);
    }

    private String parseUserIdsToStringValue(List<Long> userIds) {
        return userIds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    private void issueAndAddUserToCoupons(User user, CouponGroup couponGroup, int requiredQuantity) {
        Pageable limit = PageRequest.of(0, requiredQuantity);
        List<Coupon> coupons
                = couponRepository.findByCouponGroupAndUserIsNull(couponGroup, limit);
        couponGroup.issueAndAddUserToCoupons(user, coupons, requiredQuantity);

        couponRepository.saveAll(coupons);
    }

    private Coupon getCoupon(long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException(COUPON_ID_NOT_FOUND_EXCEPTION + couponId));
    }

    private void runJob(Job job, JobParameters jobParameters) throws JobExecutionException {
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            throw new JobExecutionException(
                    String.format(JOB_NOT_COMPLETED_MESSAGE, job.getName(), jobExecution.getStatus())
            );
        }
    }
}
