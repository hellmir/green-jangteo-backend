package com.firstone.greenjangteo.user.domain.token.repository;

import com.firstone.greenjangteo.user.domain.token.model.entity.RefreshToken;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.security.JwtTokenProvider;
import com.firstone.greenjangteo.user.testutil.UserTestObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.firstone.greenjangteo.user.model.Role.ROLE_BUYER;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RefreshTokenRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("리프레시 토큰 값을 통해 리프레시 토큰 엔티티를 조회할 수 있다.")
    @Test
    void findByToken() {
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

        RefreshToken createdRefreshToken1 = RefreshToken.from(user1.getId(), user1.getRoles().toStrings(), refreshTokenValue1);
        RefreshToken createdRefreshToken2 = RefreshToken.from(user2.getId(), user2.getRoles().toStrings(), refreshTokenValue2);
        refreshTokenRepository.saveAll(List.of(createdRefreshToken1, createdRefreshToken2));

        // when
        RefreshToken foundRefreshToken1 = refreshTokenRepository.findByToken(refreshTokenValue1).get();
        RefreshToken foundRefreshToken2 = refreshTokenRepository.findByToken(refreshTokenValue2).get();

        // then
        assertThat(foundRefreshToken1).isNotNull();
        assertThat(foundRefreshToken2).isNotNull();
        assertThat(foundRefreshToken1).isNotEqualTo(createdRefreshToken2);
    }
}
