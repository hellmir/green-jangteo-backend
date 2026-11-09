package com.firstone.greenjangteo.coupon.repository;

import com.firstone.greenjangteo.coupon.model.entity.Coupon;
import com.firstone.greenjangteo.coupon.model.entity.CouponGroup;
import com.firstone.greenjangteo.coupon.testutil.CouponTestObjectFactory;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.repository.UserRepository;
import com.firstone.greenjangteo.user.testutil.UserTestObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.firstone.greenjangteo.coupon.testutil.CouponTestConstant.*;
import static com.firstone.greenjangteo.user.model.Role.ROLE_BUYER;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CouponRepositoryTest {
    @Autowired
    private CouponGroupRepository couponGroupRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    LocalDate tomorrow = LocalDate.now().plusDays(1);

    @DisplayName("쿠폰 그룹과 회원에게 지급되지 않은 조건을 통해 일정 수량의 쿠폰 목록을 조회할 수 있다.")
    @Test
    void findByCouponGroupAndUserIsNull() {
        // given
        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, QUANTITY_TO_PROVIDE, tomorrow, EXPIRATION_PERIOD1
        );
        List<Coupon> createdCoupons = CouponTestObjectFactory.createCoupons(couponGroup);
        couponGroupRepository.save(couponGroup);
        couponRepository.saveAll(createdCoupons);

        int requiredQuantity = Integer.parseInt(QUANTITY_TO_PROVIDE);
        Pageable limit = PageRequest.of(0, requiredQuantity);

        // when
        List<Coupon> foundCoupons = couponRepository.findByCouponGroupAndUserIsNull(couponGroup, limit);

        // then
        assertThat(foundCoupons).hasSize(requiredQuantity)
                .extracting("user")
                .containsNull();
    }

    @DisplayName("회원 ID를 통해 쿠폰 목록을 조회할 수 있다.")
    @Test
    void findAllByUserId() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.name())
        );
        userRepository.save(user);

        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, QUANTITY_TO_PROVIDE, tomorrow, EXPIRATION_PERIOD1
        );

        List<Coupon> createdCoupons1 = CouponTestObjectFactory.createCoupons(couponGroup);
        List<Coupon> createdCoupons2 = CouponTestObjectFactory.createCoupons(couponGroup);

        for (Coupon createdCoupon : createdCoupons2) {
            createdCoupon.addUser(user);
        }

        couponGroupRepository.save(couponGroup);
        couponRepository.saveAll(createdCoupons1);
        couponRepository.saveAll(createdCoupons2);

        // when
        List<Coupon> coupons = couponRepository.findAllByUserId(user.getId());

        // then
        assertThat(coupons).hasSize(createdCoupons2.size())
                .extracting("user")
                .containsOnly(user);
    }

    @DisplayName("만료 시간이 전송된 시간 이전인 쿠폰들을 검색할 수 있다.")
    @Test
    void findByExpiredAtBefore() {
        // given
        LocalDateTime now = LocalDateTime.now();

        CouponGroup couponGroup1 = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, tomorrow, EXPIRATION_PERIOD1
        );
        CouponGroup couponGroup2 = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME2, AMOUNT, DESCRIPTION, ISSUE_QUANTITY3, tomorrow, EXPIRATION_PERIOD2
        );
        CouponGroup couponGroup3 = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME3, AMOUNT, DESCRIPTION, ISSUE_QUANTITY4, tomorrow, EXPIRATION_PERIOD1
        );

        List<Coupon> createdCoupons1 = CouponTestObjectFactory.createAndIssueCoupons(couponGroup1, now.minusSeconds(2));
        List<Coupon> createdCoupons2 = CouponTestObjectFactory.createAndIssueCoupons(couponGroup2, now.minusSeconds(1));
        List<Coupon> createdCoupons3 = CouponTestObjectFactory.createAndIssueCoupons(couponGroup3, now.plusSeconds(1));
        List<Coupon> createdCoupons4 = CouponTestObjectFactory.createAndIssueCoupons(couponGroup3, now.plusSeconds(2));

        couponGroupRepository.saveAll(List.of(couponGroup1, couponGroup2, couponGroup3));
        couponRepository.saveAll(createdCoupons1);
        couponRepository.saveAll(createdCoupons2);
        couponRepository.saveAll(createdCoupons3);
        couponRepository.saveAll(createdCoupons4);

        // when
        List<Coupon> coupons = couponRepository.findByExpiredAtBefore(now);

        // then
        assertThat(coupons).hasSize(createdCoupons1.size() + createdCoupons2.size())
                .extracting("couponGroup")
                .containsOnly(couponGroup1, couponGroup2);
    }

    @DisplayName("쿠폰 그룹 ID를 통해 페이징 처리한 쿠폰 목록을 검색할 수 있다.")
    @Test
    void findByCouponGroupId() {
        // given
        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, tomorrow, EXPIRATION_PERIOD1
        );

        List<Coupon> createdCoupons = CouponTestObjectFactory.createCoupons(couponGroup);
        couponGroupRepository.save(couponGroup);
        couponRepository.saveAll(createdCoupons);

        // when
        Page<Coupon> couponPage = couponRepository.findByCouponGroupId(couponGroup.getId(), PageRequest.of(0, 10));

        // then
        assertThat(couponPage).hasSize(10);
    }
}
