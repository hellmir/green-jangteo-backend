package com.firstone.greenjangteo.user.model.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.AttributeConverter;
import java.util.Objects;

import static com.firstone.greenjangteo.user.excpeption.message.BlankExceptionMessage.PASSWORD_NO_VALUE_EXCEPTION;
import static com.firstone.greenjangteo.user.excpeption.message.InvalidExceptionMessage.INVALID_PASSWORD_EXCEPTION;

public class Password {
    private final String password;

    Password(String password) {
        this.password = password;
    }

    public static Password from(String password, PasswordEncoder passwordEncoder) {
        validate(password);

        return new Password(passwordEncoder.encode(password));
    }

    protected String getValue() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password1 = (Password) o;
        return Objects.equals(password, password1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }

    public boolean matchOriginalPassword(PasswordEncoder passwordEncoder, String originalPassword) {
        return passwordEncoder.matches(originalPassword, password);
    }

    private static void validate(String password) {
        checkPasswordIsNotBlank(password);
        checkPasswordPattern(password);
    }

    private static void checkPasswordIsNotBlank(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(PASSWORD_NO_VALUE_EXCEPTION);
        }
    }

    private static void checkPasswordPattern(String password) {
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new IllegalArgumentException(INVALID_PASSWORD_EXCEPTION);
        }
    }

    public static class PasswordConverter implements AttributeConverter<Password, String> {
        @Override
        public String convertToDatabaseColumn(Password password) {
            return password == null ? null : password.password;
        }

        @Override
        public Password convertToEntityAttribute(String password) {
            return password == null ? null : new Password(password);
        }
    }
}
