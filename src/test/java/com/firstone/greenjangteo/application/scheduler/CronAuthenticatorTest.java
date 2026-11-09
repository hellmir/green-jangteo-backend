package com.firstone.greenjangteo.application.scheduler;

import com.firstone.greenjangteo.application.exception.InvalidCronSecretException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronAuthenticatorTest {
    private static final String SECRET = "valid-cron-secret-0123456789";

    @DisplayName("Bearer 접두사와 일치하는 시크릿이면 통과한다.")
    @Test
    void authenticate() {
        // given
        CronAuthenticator cronAuthenticator = new CronAuthenticator(SECRET);

        // when, then
        assertThatCode(() -> cronAuthenticator.authenticate("Bearer " + SECRET)).doesNotThrowAnyException();
    }

    @DisplayName("헤더가 없거나 값이 일치하지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {SECRET, "Bearer wrong", "bearer " + SECRET, "Bearer " + SECRET + "x", "Bearer "})
    void rejectInvalidAuthorization(String authorization) {
        // given
        CronAuthenticator cronAuthenticator = new CronAuthenticator(SECRET);

        // when, then
        assertThatThrownBy(() -> cronAuthenticator.authenticate(authorization))
                .isInstanceOf(InvalidCronSecretException.class);
    }

    @DisplayName("서버에 시크릿이 설정되지 않았으면 빈 토큰으로도 통과하지 못한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectAllWhenSecretIsNotConfigured(String configuredSecret) {
        // given
        CronAuthenticator cronAuthenticator = new CronAuthenticator(configuredSecret);

        // when, then
        assertThatThrownBy(() -> cronAuthenticator.authenticate("Bearer "))
                .isInstanceOf(InvalidCronSecretException.class);
        assertThatThrownBy(() -> cronAuthenticator.authenticate("Bearer null"))
                .isInstanceOf(InvalidCronSecretException.class);
    }
}
