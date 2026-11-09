package com.firstone.greenjangteo.product.exception;

import com.firstone.greenjangteo.exception.AbstractGeneralException;

public class ProductSearchUnavailableException extends AbstractGeneralException {
    public ProductSearchUnavailableException() {
        super(ErrorCode.PRODUCT_SEARCH_IS_UNAVAILABLE.getDescription());
    }

    @Override
    public int getStatusCode() {
        return ErrorCode.PRODUCT_SEARCH_IS_UNAVAILABLE.getHttpStatus().value();
    }
}
