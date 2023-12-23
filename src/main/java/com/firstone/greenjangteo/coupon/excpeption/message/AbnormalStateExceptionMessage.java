package com.firstone.greenjangteo.coupon.excpeption.message;

public class AbnormalStateExceptionMessage {
    public static final String INCONSISTENT_COUPON_SIZE_EXCEPTION
            = "지급 예정 쿠폰 수량이 필요로 하는 쿠폰 수량과 일치하지 않습니다. 지급 예정 쿠폰 수량: ";
    public static final String INCONSISTENT_COUPON_SIZE_EXCEPTION_REQUIRED_QUANTITY = ", 필요로 하는 쿠폰 수량: ";
    public static final String INSUFFICIENT_REMAINING_QUANTITY_EXCEPTION
            = "쿠폰 그룹의 잔여 쿠폰 수량이 지급 예정 쿠폰 수량보다 부족합니다. 잔여 쿠폰 수량: ";
    public static final String INSUFFICIENT_REMAINING_QUANTITY_EXCEPTION_QUANTITY_TO_PROVIDE = ", 지급 예정 쿠폰 수량: ";
    public static final String ALREADY_GIVEN_COUPON_EXCEPTION = "이미 다른 사용자에게 지급된 쿠폰입니다.";
}
