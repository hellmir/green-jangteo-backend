package com.firstone.greenjangteo.user.service;

import com.firstone.greenjangteo.user.form.SignUpForm;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.model.security.CustomUserDetails;
import com.firstone.greenjangteo.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.firstone.greenjangteo.user.model.Role.*;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.*;
import static com.firstone.greenjangteo.user.testutil.UserTestObjectFactory.enterUserForm;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserDetailsServiceTest {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("가입된 사용자의 인증 정보와 권한 정보를 불러 올 수 있다.")
    @Test
    void loadUserByUsername() {
        // given
        List<String> roles1 = List.of(ROLE_SELLER.toString());
        List<String> roles2 = List.of(ROLE_BUYER.toString());
        List<String> roles3 = List.of(ROLE_ADMIN.toString());
        List<String> roles4 = List.of(ROLE_BUYER.toString(), ROLE_ADMIN.toString());

        SignUpForm signUpForm1 = enterUserForm
                (EMAIL1, USERNAME1, PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, roles1);

        SignUpForm signUpForm2 = enterUserForm
                (EMAIL2, USERNAME2, PASSWORD2, PASSWORD2, FULL_NAME2, PHONE2, roles2);

        SignUpForm signUpForm3 = enterUserForm
                (EMAIL3, USERNAME3, PASSWORD3, PASSWORD3, FULL_NAME3, PHONE3, roles3);

        SignUpForm signUpForm4 = enterUserForm
                (EMAIL4, USERNAME4, PASSWORD4, PASSWORD4, FULL_NAME4, PHONE4, roles4);

        authenticationService.signUpUser(signUpForm1);
        authenticationService.signUpUser(signUpForm2);
        authenticationService.signUpUser(signUpForm3);
        authenticationService.signUpUser(signUpForm4);

        List<User> users = userRepository.findAll();

        String userId1 = String.valueOf(users.get(0).getId());
        String userId2 = String.valueOf(users.get(1).getId());
        String userId3 = String.valueOf(users.get(2).getId());
        String userId4 = String.valueOf(users.get(3).getId());

        // when
        UserDetails user1 = userDetailsService.loadUserByUsername(userId1);
        UserDetails user2 = userDetailsService.loadUserByUsername(userId2);
        UserDetails user3 = userDetailsService.loadUserByUsername(userId3);
        UserDetails user4 = userDetailsService.loadUserByUsername(userId4);

        // then
        assertThat(user1.getUsername()).isEqualTo(userId1);
        assertThat(user1.getAuthorities()).isEqualTo(new CustomUserDetails(users.get(0)).getAuthorities());

        assertThat(user2.getUsername()).isEqualTo(userId2);
        assertThat(user2.getAuthorities()).isEqualTo(new CustomUserDetails(users.get(1)).getAuthorities());

        assertThat(user3.getUsername()).isEqualTo(userId3);
        assertThat(user3.getAuthorities()).isEqualTo(new CustomUserDetails(users.get(2)).getAuthorities());

        assertThat(user4.getUsername()).isEqualTo(userId4);
        assertThat(user4.getAuthorities()).isEqualTo(new CustomUserDetails(users.get(3)).getAuthorities());
    }
}
