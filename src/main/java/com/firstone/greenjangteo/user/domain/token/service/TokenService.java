package com.firstone.greenjangteo.user.domain.token.service;

import com.firstone.greenjangteo.user.domain.token.model.entity.RefreshToken;
import com.firstone.greenjangteo.user.domain.token.repository.RefreshTokenRepository;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    static final String INVALID_TOKEN_EXCEPTION_MESSAGE = "유효하지 않은 리프레시 토큰입니다.";

    public String issueAccessToken(User user) {
        String accessToken
                = jwtTokenProvider.generateAccessToken(String.valueOf(user.getId()), user.getRoles().toStrings());

        return accessToken;
    }

    public String issueRefreshToken(User user) {
        String refreshToken
                = jwtTokenProvider.generateRefreshToken(String.valueOf(user.getId()), user.getRoles().toStrings());
        refreshTokenRepository.save(RefreshToken.from(user.getId(), user.getRoles().toStrings(), refreshToken));

        return refreshToken;
    }

    public String issueNewAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = getRefreshToken(refreshTokenValue);
        return refreshToken.createNewAccessToken(jwtTokenProvider);
    }

    private RefreshToken getRefreshToken(String refreshTokenValue) {
        return refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_EXCEPTION_MESSAGE));
    }
}
