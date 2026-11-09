package com.firstone.greenjangteo.coupon.model.entity;

import com.firstone.greenjangteo.coupon.exception.serious.AlreadyProvidedCouponException;
import com.firstone.greenjangteo.coupon.model.ExpirationPeriod;
import com.firstone.greenjangteo.coupon.testutil.CouponTestObjectFactory;
import com.firstone.greenjangteo.user.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.firstone.greenjangteo.coupon.exception.message.AbnormalStateExceptionMessage.ALREADY_GIVEN_COUPON_EXCEPTION;
import static com.firstone.greenjangteo.coupon.testutil.CouponTestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CouponTest {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);
    LocalDateTime now = LocalDateTime.now();

    @DisplayName("쿠폰을 생성할 수 있다.")
    @Test
    void create() {
        // given
        CouponGroup couponGroup
                = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, tomorrow, EXPIRATION_PERIOD1
        );

        // when
        Coupon coupon = new Coupon(couponGroup, now);

        // then
        assertThat(coupon.getCouponGroup()).isEqualTo(couponGroup);
        assertThat(coupon.getCreatedAt()).isEqualTo(now);
    }

    @DisplayName("동일한 내부 값들을 전송하면 동등한 Coupon 인스턴스를 생성한다.")
    @Test
    void ofSameValue() {
        // given
        CouponGroup couponGroup
                = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, tomorrow, EXPIRATION_PERIOD1
        );

        // when
        Coupon coupon1 = new Coupon(couponGroup, now);
        Coupon coupon2 = new Coupon(couponGroup, now);

        // then
        assertThat(coupon1).isEqualTo(coupon2);
        assertThat(coupon1.hashCode()).isEqualTo(coupon2.hashCode());
    }

    @DisplayName("다른 내부 값들을 전송하면 동등하지 않은 Coupon 인스턴스를 생성한다.")
    @Test
    void ofDifferentValue() {
        // given
        CouponGroup couponGroup1
                = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, tomorrow, EXPIRATION_PERIOD1
        );

        CouponGroup couponGroup2
                = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, ISSUE_QUANTITY1, dayAfterTomorrow, EXPIRATION_PERIOD1
        );

        // when
        Coupon coupon1 = new Coupon(couponGroup1, now);
        Coupon coupon2 = new Coupon(couponGroup2, now);

        // then
        assertThat(coupon1).isNotEqualTo(coupon2);
        assertThat(coupon1.hashCode()).isNotEqualTo(coupon2.hashCode());
    }

    @DisplayName("쿠폰을 발행하고 회원에게 지급한다.")
    @Test
    void issueAndAddUser() {
        // given
        User user = mock(User.class);
        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, QUANTITY_TO_PROVIDE, tomorrow, EXPIRATION_PERIOD1
        );
        List<Coupon> coupons = CouponTestObjectFactory.createCoupons(couponGroup);

        int requiredQuantity = Integer.parseInt(QUANTITY_TO_PROVIDE);

        // when
        for (Coupon coupon : coupons) {
            coupon.issueAndAddUser(user, ExpirationPeriod.of(EXPIRATION_PERIOD1));
        }

        // then
        long expirationPeriod = Long.parseLong(EXPIRATION_PERIOD1);

        assertThat(coupons).hasSize(requiredQuantity)
                .allSatisfy(coupon -> {
                    assertThat(coupon.getUser()).isEqualTo(user);
                    assertThat(coupon.getExpiredAt()).isEqualTo(coupon.getIssuedAt().plusDays(expirationPeriod));
                });
    }

    @DisplayName("회원이 비어 있지 않은 상태에서 쿠폰을 지급하려 하면 AlreadyProvidedCouponException이 발생한다.")
    @Test
    void addNotNullUser() {
        // given
        User user = mock(User.class);
        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                COUPON_NAME1, AMOUNT, DESCRIPTION, QUANTITY_TO_PROVIDE, tomorrow, EXPIRATION_PERIOD1
        );
        List<Coupon> coupons = CouponTestObjectFactory.createCoupons(couponGroup);

        int requiredQuantity = Integer.parseInt(QUANTITY_TO_PROVIDE);
        couponGroup.issueAndAddUserToCoupons(user, coupons, requiredQuantity);

        // when, then
        for (Coupon coupon : coupons) {
            assertThatThrownBy(() -> coupon.issueAndAddUser(user, ExpirationPeriod.of(EXPIRATION_PERIOD1)))
                    .isInstanceOf(AlreadyProvidedCouponException.class)
                    .hasMessage(ALREADY_GIVEN_COUPON_EXCEPTION + user.getId());
        }
    }
}
