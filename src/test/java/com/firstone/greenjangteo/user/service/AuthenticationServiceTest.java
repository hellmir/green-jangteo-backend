package com.firstone.greenjangteo.user.service;

import com.firstone.greenjangteo.coupon.model.entity.Coupon;
import com.firstone.greenjangteo.coupon.model.entity.CouponGroup;
import com.firstone.greenjangteo.coupon.repository.CouponGroupRepository;
import com.firstone.greenjangteo.coupon.repository.CouponRepository;
import com.firstone.greenjangteo.coupon.testutil.CouponTestObjectFactory;
import com.firstone.greenjangteo.product.domain.model.Category;
import com.firstone.greenjangteo.product.domain.model.Product;
import com.firstone.greenjangteo.product.repository.CategoryRepository;
import com.firstone.greenjangteo.product.repository.ProductRepository;
import com.firstone.greenjangteo.user.domain.store.model.StoreName;
import com.firstone.greenjangteo.user.domain.store.model.entity.Store;
import com.firstone.greenjangteo.user.domain.store.repository.StoreRepository;
import com.firstone.greenjangteo.user.domain.store.testutil.StoreTestObjectFactory;
import com.firstone.greenjangteo.user.dto.request.DeleteRequestDto;
import com.firstone.greenjangteo.user.dto.request.EmailRequestDto;
import com.firstone.greenjangteo.user.dto.request.PasswordUpdateRequestDto;
import com.firstone.greenjangteo.user.dto.request.PhoneRequestDto;
import com.firstone.greenjangteo.user.excpeption.general.DuplicateUserException;
import com.firstone.greenjangteo.user.excpeption.general.DuplicateUsernameException;
import com.firstone.greenjangteo.user.excpeption.significant.IncorrectPasswordException;
import com.firstone.greenjangteo.user.form.SignInForm;
import com.firstone.greenjangteo.user.form.SignUpForm;
import com.firstone.greenjangteo.user.model.Email;
import com.firstone.greenjangteo.user.model.Phone;
import com.firstone.greenjangteo.user.model.Role;
import com.firstone.greenjangteo.user.model.Username;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.repository.UserRepository;
import com.firstone.greenjangteo.user.testutil.UserTestObjectFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.firstone.greenjangteo.coupon.testutil.CouponTestConstant.*;
import static com.firstone.greenjangteo.coupon.utility.CouponInformationConstant.NEW_MEMBER_DISCOUNT_COUPON_NAME;
import static com.firstone.greenjangteo.coupon.utility.CouponInformationConstant.NEW_MEMBER_DISCOUNT_COUPON_QUANTITY;
import static com.firstone.greenjangteo.user.domain.store.testutil.StoreTestConstant.*;
import static com.firstone.greenjangteo.user.excpeption.message.DuplicateExceptionMessage.*;
import static com.firstone.greenjangteo.user.excpeption.message.IncorrectPasswordExceptionMessage.INCORRECT_PASSWORD_EXCEPTION;
import static com.firstone.greenjangteo.user.excpeption.message.InvalidExceptionMessage.*;
import static com.firstone.greenjangteo.user.excpeption.message.NotFoundExceptionMessage.*;
import static com.firstone.greenjangteo.user.model.Role.ROLE_BUYER;
import static com.firstone.greenjangteo.user.model.Role.ROLE_SELLER;
import static com.firstone.greenjangteo.user.testutil.UserTestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CouponGroupRepository couponGroupRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("올바른 회원 가입 양식을 전송하면 회원 가입을 할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, Abcd1234!, 홍길동, 01012345678, ROLE_BUYER",
            "abcd@abcd.com, person2, Abcd12345!, 고길동, 01012345679, ROLE_SELLER",
            "abcd@abcde.com, person3, Abcd123456!, 김길동, 01012345680, ROLE_ADMIN"
    })
    void signUpUser(String email, String username, String password, String fullName,
                    String phone, String role) {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm
                (email, username, password, password, fullName, phone, List.of(role));

        // when
        User signedUpuser = authenticationService.signUpUser(signUpForm);
        User foundUser = userRepository.findByEmail(Email.of(email)).get();

        // then
        assertThat(foundUser.getEmail()).isEqualTo(Email.of(email));
        assertThat(foundUser.getUsername()).isEqualTo(Username.of(username));
        assertThat(foundUser.getRoles().get(0).toString()).isEqualTo(role);
        assertThat(foundUser.getPhone()).isEqualTo(Phone.of(phone));
        assertThat(foundUser.getCreatedAt()).isEqualTo(signedUpuser.getCreatedAt());
    }

    @DisplayName("올바른 회원 가입 양식을 전송하면 여러 권한을 가진 회원으로 가입할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, Abcd1234!, 홍길동, 01012345678, ROLE_BUYER, ROLE_SELLER",
            "abcd@abcd.com, person2, Abcd12345!, 고길동, 01012345679, ROLE_SELLER, ROLE_BUYER",
            "abcd@abcde.com, person3, Abcd123456!, 김길동, 01012345680, ROLE_BUYER, ROLE_ADMIN"
    })
    void signUpUserWithMultipleRoles(String email, String username, String password, String fullName,
                                     String phone, String role1, String role2) {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm
                (email, username, password, password, fullName, phone, List.of(role1, role2));

        // when
        User signedUpuser = authenticationService.signUpUser(signUpForm);
        User foundUser = userRepository.findByEmail(Email.of(email)).get();

        // then
        assertThat(foundUser.getEmail()).isEqualTo(Email.of(email));
        assertThat(foundUser.getUsername()).isEqualTo(Username.of(username));
        assertThat(foundUser.getRoles().get(0).toString()).isEqualTo(role1);
        assertThat(foundUser.getRoles().get(1).toString()).isEqualTo(role2);
        assertThat(foundUser.getPhone()).isEqualTo(Phone.of(phone));
        assertThat(signedUpuser.getCreatedAt()).isEqualTo(foundUser.getCreatedAt());
    }

    @DisplayName("회원 가입 시 비밀번호를 암호화 해 저장할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, Abcd1234!, 홍길동, 01012345678, ROLE_BUYER, ROLE_SELLER",
            "abcd@abcd.com, person2, Abcd12345!, 고길동, 01012345679, ROLE_SELLER, ROLE_BUYER",
            "abcd@abcde.com, person3, Abcd123456!, 김길동, 01012345680, ROLE_BUYER, ROLE_ADMIN"
    })
    void signUpUserWithEncodingPassword(String email, String username, String password, String fullName,
                                        String phone, Role role1, Role role2) {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm
                (email, username, password, password, fullName, phone, List.of(role1.toString(), role2.toString()));

        // when
        authenticationService.signUpUser(signUpForm);
        User user = userRepository.findByEmail(Email.of(email)).get();

        // then
        assertThat(user.getPassword()).isNotEqualTo(password);
        assertThat(user.getPassword().matchOriginalPassword(passwordEncoder, password)).isTrue();
    }

    @DisplayName("비밀번호와 재입력 비밀번호를 다르게 입력하는 경우 IncorrectPasswordException이 발생한다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, Abcd1234!, Abcd12345!, 홍길동, 01012345678, ROLE_BUYER, ROLE_SELLER",
            "abcd@abcd.com, person2, Abcd12345!, Abcd1234!, 고길동, 01012345679, ROLE_SELLER, ROLE_BUYER",
            "abcd@abcde.com, person3, Abcd123456!, Abcd1234!, 김길동, 01012345680, ROLE_BUYER, ROLE_ADMIN"
    })
    void signUpUserWithWrongPasswordConfirm(String email, String username, String password, String passwordConfirm,
                                            String fullName, String phone, Role role1, Role role2) {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm
                (email, username, password, passwordConfirm,
                        fullName, phone, List.of(role1.toString(), role2.toString()));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("중복된 이메일을 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicateEmail() {
        // given
        SignUpForm signUpForm1 = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME2,
                PASSWORD2, PASSWORD2, FULL_NAME2, PHONE2, List.of(ROLE_SELLER.toString()));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage(DUPLICATE_EMAIL_EXCEPTION + signUpForm2.getEmail());
    }

    @DisplayName("중복된 전화번호를 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicatePhone() {
        // given
        SignUpForm signUpForm1 = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = UserTestObjectFactory.enterUserForm(EMAIL2, USERNAME2,
                PASSWORD2, PASSWORD2, FULL_NAME2, PHONE1, List.of(ROLE_SELLER.toString()));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage(DUPLICATE_PHONE_EXCEPTION + signUpForm2.getPhone());
    }

    @DisplayName("중복된 사용자 이름을 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicateUsername() {
        // given
        SignUpForm signUpForm1 = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = UserTestObjectFactory.enterUserForm(EMAIL2, USERNAME1, PASSWORD2, PASSWORD2,
                FULL_NAME2, PHONE2, List.of(ROLE_SELLER.toString()));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage(DUPLICATE_USERNAME_EXCEPTION + signUpForm2.getUsername());
    }

    @DisplayName("판매자가 회원 가입하면 가게가 함께 생성된다.")
    @Test
    void createStoreWhenSellerSignedUp() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(
                EMAIL1, USERNAME1, PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_SELLER.toString())
        );

        StoreName enteredStoreName = StoreName.of(signUpForm.getStoreName());

        // when
        User user = authenticationService.signUpUser(signUpForm);
        Optional<Store> store = storeRepository.findById(user.getId());

        // then
        assertThat((store).isPresent()).isTrue();
        assertThat(store.get().getStoreName()).isEqualTo(enteredStoreName);
    }

    @DisplayName("구매자가 회원 가입하면 신규회원 할인 쿠폰이 3장 지급된다.")
    @Test
    @Transactional
    void provideCouponsToSignedUpBuyer() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(
                EMAIL1, USERNAME1, PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CouponGroup couponGroup = CouponTestObjectFactory.createCouponGroup(
                NEW_MEMBER_DISCOUNT_COUPON_NAME, AMOUNT, DESCRIPTION, ISSUE_QUANTITY3, tomorrow, EXPIRATION_PERIOD1
        );

        List<Coupon> coupons = CouponTestObjectFactory.createCoupons(couponGroup);
        couponGroupRepository.save(couponGroup);
        couponRepository.saveAll(coupons);

        // when
        User user = authenticationService.signUpUser(signUpForm);
        entityManager.flush();
        entityManager.refresh(user);

        // then
        List<Coupon> providedCoupons = user.getCoupons();
        assertThat(providedCoupons.size()).isEqualTo(NEW_MEMBER_DISCOUNT_COUPON_QUANTITY);

        for (Coupon providedCoupon : providedCoupons) {
            assertThat(providedCoupon.getUser()).isEqualTo(user);
            assertThat(providedCoupon.getCouponGroup().getCouponName()).isEqualTo(NEW_MEMBER_DISCOUNT_COUPON_NAME);
        }
    }

    @DisplayName("가입된 이메일 주소 또는 사용자 이름과 올바른 비밀번호를 전송하면 로그인을 할 수 있다.")
    @Test
    void signInUser() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL1, PASSWORD1);
        SignInForm signInForm2 = new SignInForm(USERNAME1, PASSWORD1);

        // when
        User user1 = authenticationService.signInUser(signInForm1);
        User user2 = authenticationService.signInUser(signInForm2);

        // then
        assertThat(user1.getUsername()).isEqualTo(Username.of(USERNAME1));
        assertThat(user2.getUsername()).isEqualTo(Username.of(USERNAME1));
    }

    @DisplayName("존재하지 않는 이메일이나 사용자 이름으로 로그인하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void signInUserWithNonExistentEmailOrUsername() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL2, PASSWORD1);
        SignInForm signInForm2 = new SignInForm(USERNAME2, PASSWORD1);

        // when, then
        assertThatThrownBy(() -> authenticationService.signInUser(signInForm1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(EMAIL_NOT_FOUND_EXCEPTION + signInForm1.getEmailOrUsername());

        assertThatThrownBy(() -> authenticationService.signInUser(signInForm2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(USERNAME_NOT_FOUND_EXCEPTION + signInForm2.getEmailOrUsername());
    }

    @DisplayName("잘못된 비밀번호를 통해 로그인하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void signInUserWithWrongPassword() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString()));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL1, PASSWORD2);
        SignInForm signInForm2 = new SignInForm(USERNAME1, PASSWORD3);

        // when, then
        assertThatThrownBy(() -> authenticationService.signInUser(signInForm1))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);

        assertThatThrownBy(() -> authenticationService.signInUser(signInForm2))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("비밀번호와 변경할 이메일 주소를 입력해 이메일 주소를 변경할 수 있다.")
    @Test
    void updateEmail() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        EmailRequestDto emailRequestDto = EmailRequestDto.builder()
                .password(PASSWORD1)
                .email(EMAIL2)
                .build();

        // when
        authenticationService.updateEmail(user.getId(), emailRequestDto);

        // then
        assertThat(user.getEmail()).isNotEqualTo(Email.of(EMAIL1));
        assertThat(user.getEmail()).isEqualTo(Email.of(EMAIL2));
    }

    @DisplayName("잘못된 비밀번호를 통해 이메일 주소를 변경하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void updateEmailWithWrongPassword() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        EmailRequestDto emailRequestDto = EmailRequestDto.builder()
                .password(PASSWORD2)
                .email(EMAIL2)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updateEmail(user.getId(), emailRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("유효하지 않은 이메일 주소를 통해 이메일 주소를 변경하려 하면 IllegalArgumentException이 발생한다.")
    @ParameterizedTest
    @CsvSource({
            "abcD1!", "1234!abcde", "AbCdE12345", "!@1234ABCDE"
    })
    void updateEmailWithInvalidEmail(String email) {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        EmailRequestDto emailRequestDto = EmailRequestDto.builder()
                .password(PASSWORD1)
                .email(email)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updateEmail(user.getId(), emailRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_EMAIL_EXCEPTION);
    }

    @DisplayName("비밀번호와 변경할 전화번호를 입력해 전화번호를 변경할 수 있다.")
    @Test
    void updatePhone() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PhoneRequestDto phoneRequestDto = PhoneRequestDto.builder()
                .password(PASSWORD1)
                .phone(PHONE2)
                .build();

        // when
        authenticationService.updatePhone(user.getId(), phoneRequestDto);

        // then
        assertThat(user.getPhone()).isNotEqualTo(Phone.of(PHONE1));
        assertThat(user.getPhone()).isEqualTo(Phone.of(PHONE2));
    }

    @DisplayName("잘못된 비밀번호를 통해 전화번호를 변경하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void updatePhoneWithWrongPassword() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PhoneRequestDto phoneRequestDto = PhoneRequestDto.builder()
                .password(PASSWORD2)
                .phone(PHONE2)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updatePhone(user.getId(), phoneRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("유효하지 않은 전화번호를 통해 전화번호를 변경하려 하면 IllegalArgumentException이 발생한다.")
    @ParameterizedTest
    @CsvSource({
            "01112345678", "010123456789", "010123$5678", "010a2345678"
    })
    void updatePhoneWithInvalidPhone(String phone) {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PhoneRequestDto phoneRequestDto = PhoneRequestDto.builder()
                .password(PASSWORD1)
                .phone(phone)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updatePhone(user.getId(), phoneRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_PHONE_EXCEPTION);
    }

    @DisplayName("현재 비밀번호와 변경할 비밀번호, 변경할 비밀번호 확인을 입력해 비밀번호를 변경할 수 있다.")
    @Test
    void updatePassword() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PasswordUpdateRequestDto passwordUpdateRequestDto = PasswordUpdateRequestDto.builder()
                .currentPassword(PASSWORD1)
                .passwordToChange(PASSWORD2)
                .passwordToChangeConfirm(PASSWORD2)
                .build();

        // when
        authenticationService.updatePassword(user.getId(), passwordUpdateRequestDto);

        // then
        assertThat(user.getPassword().matchOriginalPassword(passwordEncoder, PASSWORD1)).isFalse();
        assertThat(user.getPassword().matchOriginalPassword(passwordEncoder, PASSWORD2)).isTrue();
    }

    @DisplayName("잘못된 현재 비밀번호를 통해 비밀번호를 변경하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void updatePasswordWithWrongCurrentPassword() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PasswordUpdateRequestDto passwordUpdateRequestDto = PasswordUpdateRequestDto.builder()
                .currentPassword(PASSWORD2)
                .passwordToChange(PASSWORD2)
                .passwordToChangeConfirm(PASSWORD2)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updatePassword(user.getId(), passwordUpdateRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("유효하지 않은 비밃번호를 통해 비밀번호를 변경하려 하면 IllegalArgumentException이 발생한다.")
    @ParameterizedTest
    @CsvSource({
            "abcD1!", "1234!abcde", "AbCdE12345", "!@1234ABCDE"
    })
    void updatePasswordWithInvalidPassword(String password) {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PasswordUpdateRequestDto passwordUpdateRequestDto = PasswordUpdateRequestDto.builder()
                .currentPassword(PASSWORD1)
                .passwordToChange(password)
                .passwordToChangeConfirm(password)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updatePassword(user.getId(), passwordUpdateRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_PASSWORD_EXCEPTION);
    }

    @DisplayName("일치하지 않는 비밀번호 확인을 통해 비밀번호를 변경하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void updatePasswordWithWrongPasswordConfirm() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        PasswordUpdateRequestDto passwordUpdateRequestDto = PasswordUpdateRequestDto.builder()
                .currentPassword(PASSWORD1)
                .passwordToChange(PASSWORD2)
                .passwordToChangeConfirm(PASSWORD3)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService.updatePassword(user.getId(), passwordUpdateRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("비밀번호 인증을 통해 회원을 탈퇴할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, Abcd1234!, 홍길동, 01012345678, ROLE_BUYER",
            "abcd@abcd.com, person2, Abcd12345!, 고길동, 01012345679, ROLE_ADMIN",
            "abcd@abcde.com, person3, Abcd123456!, 김길동, 01012345680, ROLE_BUYER"
    })
    void deleteUser(
            String email, String username, String password,
            String fullName, String phone, String role
    ) {
        // given
        User user = UserTestObjectFactory.createUser(
                email, username, password, passwordEncoder, fullName, phone, List.of(role)
        );
        userRepository.save(user);

        Long userId = user.getId();
        storeRepository.save(Store.of(userId, STORE_NAME1));

        DeleteRequestDto deleteRequestDto = new DeleteRequestDto(password);

        // when
        authenticationService.deleteUser(userId, deleteRequestDto);

        // then
        assertThat(userRepository.findById(userId).isPresent()).isFalse();
    }

    @DisplayName("존재하지 않는 회원 ID로 회원을 탈퇴하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void deleteUserByNonExistentId() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        Long userId = user.getId() + 1;

        DeleteRequestDto deleteRequestDto = new DeleteRequestDto(PASSWORD1);

        // when, then
        assertThatThrownBy(() -> authenticationService
                .deleteUser(userId, deleteRequestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(USER_ID_NOT_FOUND_EXCEPTION + userId);
    }

    @DisplayName("일치하지 않는 비밀번호로 회원을 탈퇴하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void deleteUserWithWrongPassword() {
        // given
        User user = UserTestObjectFactory.createUser(
                EMAIL1, USERNAME1, PASSWORD1, passwordEncoder, FULL_NAME1, PHONE1, List.of(ROLE_BUYER.toString())
        );
        userRepository.save(user);

        Long userId = user.getId();

        DeleteRequestDto deleteRequestDto = new DeleteRequestDto(PASSWORD2);

        // when, then
        assertThatThrownBy(() -> authenticationService
                .deleteUser(userId, deleteRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage(INCORRECT_PASSWORD_EXCEPTION);
    }

    @DisplayName("판매자가 회원을 탈퇴하면 판매자의 가게가 함께 삭제된다.")
    @Test
    void deleteStore() {
        // given
        SignUpForm signUpForm = UserTestObjectFactory.enterUserForm(
                EMAIL1, USERNAME1, PASSWORD1, PASSWORD1, FULL_NAME1, PHONE1, List.of(ROLE_SELLER.toString())
        );
        User user = authenticationService.signUpUser(signUpForm);
        Store store = storeRepository.findById(user.getId()).get();

        createProductsAndCategory(store);

        // when
        authenticationService.deleteUser(user.getId(), new DeleteRequestDto(PASSWORD1));

        // then
        assertThat(storeRepository.findById(user.getId()).isPresent()).isFalse();
    }

    private void createProductsAndCategory(Store store) {
        Product product1 = StoreTestObjectFactory.createProduct(store, PRODUCT_NAME1, PRICE1, INVENTORY1);
        Product product2 = StoreTestObjectFactory.createProduct(store, PRODUCT_NAME2, PRICE2, INVENTORY2);
        Category category = StoreTestObjectFactory.createCategory();
        productRepository.saveAll(List.of(product1, product2));
        categoryRepository.save(category);

        entityManager.flush();
        entityManager.refresh(store);
    }
}
