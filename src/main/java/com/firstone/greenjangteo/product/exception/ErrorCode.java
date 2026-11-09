package com.firstone.greenjangteo.product.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PRODUCT_IS_EMPTY(HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다."),
    PRODUCT_IS_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 정보가 존재하지 않습니다."),
    REVIEW_IS_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보가 존재하지 않습니다."),
    PRODUCT_SEARCH_IS_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "상품 검색 서비스를 사용할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String description;
}
