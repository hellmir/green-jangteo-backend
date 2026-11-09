package com.firstone.greenjangteo.application.exception;

import com.firstone.greenjangteo.exception.AbstractGeneralException;
import org.springframework.http.HttpStatus;

public class InvalidCronSecretException extends AbstractGeneralException {
    private static final String INVALID_CRON_SECRET = "크론 인증에 실패했습니다.";

    public InvalidCronSecretException() {
        super(INVALID_CRON_SECRET);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
