package com.firstone.greenjangteo.user.domain.token.service;

import com.firstone.greenjangteo.user.domain.token.model.entity.RefreshToken;
import com.firstone.greenjangteo.user.domain.token.repository.RefreshTokenRepository;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.security.JwtTokenProvider;
import com.firstone.greenjangteo.user.testutil.UserTestObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.firstone.greenjangteo.user.domain.token.service.TokenService.INVALID_TOKEN_EXCEPTION_MESSAGE;
import static com.firstone.greenjangteo.user.model.Role.ROLE_BUYER;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TokenServiceTest {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("엑세스 토큰을 발급할 수 있다.")
    @Test
    void issueAccessToken() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );

        // when
        String accessToken = tokenService.issueAccessToken(user);

        // then
        assertThat(accessToken).isNotBlank();
    }

    @DisplayName("리프레시 토큰을 발급하고 저장할 수 있다.")
    @Test
    void issueRefreshToken() {
        // given
        User user = UserTestObjectFactory.createUser(
                1L, EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );

        // when
        String createdRefreshToken = tokenService.issueRefreshToken(user);
        String foundRefreshToken = refreshTokenRepository.findAll().get(0).getTokenValue();

        // then
        assertThat(createdRefreshToken).isNotBlank();
        assertThat(foundRefreshToken).isEqualTo(createdRefreshToken);
    }

    @DisplayName("리프레시 토큰 값을 통해 새로운 엑세스 토큰을 발급할 수 있다.")
    @Test
    void issueNewAccessToken() {
        // given
        User user1 = UserTestObjectFactory.createUser(
                1L, EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        User user2 = UserTestObjectFactory.createUser(
                2L, EMAIL2, USERNAME2, PASSWORD2, passwordEncoder, FULL_NAME2, PHONE2, List.of(ROLE_BUYER.toString())
        );

        String refreshTokenValue1
                = jwtTokenProvider.generateRefreshToken(user1.getId().toString(), user1.getRoles().toStrings());
        String refreshTokenValue2
                = jwtTokenProvider.generateRefreshToken(user2.getId().toString(), user2.getRoles().toStrings());

        RefreshToken refreshToken1 = RefreshToken.from(user1.getId(), user1.getRoles().toStrings(), refreshTokenValue1);
        RefreshToken refreshToken2 = RefreshToken.from(user2.getId(), user2.getRoles().toStrings(), refreshTokenValue2);
        refreshTokenRepository.saveAll(List.of(refreshToken1, refreshToken2));

        // when
        String accessToken1 = tokenService.issueNewAccessToken(refreshTokenValue1);
        String accessToken2 = tokenService.issueNewAccessToken(refreshTokenValue2);

        // then
        assertThat(accessToken1).isNotBlank();
        assertThat(accessToken2).isNotBlank();
        assertThat(accessToken1).isNotEqualTo(accessToken2);
    }

    @DisplayName("존재하지 않는 리프레시 토큰 값을 통해 엑세스 토큰을 발급하려 하면 IllegalArgumentException이 발생한다.")
    @Test
    void issueNewAccessTokenFromNonExistentRefreshTokenValue() {
        // given
        User user = UserTestObjectFactory.createUser(
                1L, EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );

        String refreshTokenValue
                = jwtTokenProvider.generateRefreshToken(user.getId().toString(), user.getRoles().toStrings());

        // when, then
        assertThatThrownBy(() -> tokenService.issueNewAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_TOKEN_EXCEPTION_MESSAGE);
    }

    @DisplayName("리프레시 토큰 값은 로그에 남기지 않는다.")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void doesNotLogRefreshTokenValue(CapturedOutput output) {
        // given
        String refreshTokenValue = "refresh-token-value-to-hide";

        // when
        assertThatThrownBy(() -> tokenService.issueNewAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        assertThat(output.getOut()).doesNotContain(refreshTokenValue);
    }
}
