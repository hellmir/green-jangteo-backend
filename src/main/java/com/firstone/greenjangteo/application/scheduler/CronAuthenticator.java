package com.firstone.greenjangteo.application.scheduler;

import com.firstone.greenjangteo.application.exception.InvalidCronSecretException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CronAuthenticator {
    private static final String TOKEN_PREFIX = "Bearer ";

    private final String cronSecret;

    public CronAuthenticator(@Value("${cron.secret}") String cronSecret) {
        this.cronSecret = cronSecret;
    }

    public void authenticate(String authorization) {
        if (!StringUtils.hasText(cronSecret) || !StringUtils.hasText(authorization)) {
            throw new InvalidCronSecretException();
        }

        byte[] expected = (TOKEN_PREFIX + cronSecret).getBytes(StandardCharsets.UTF_8);
        byte[] actual = authorization.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, actual)) {
            throw new InvalidCronSecretException();
        }
    }
}
