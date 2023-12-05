package com.firstone.greenjangteo.user.testutil;

import com.firstone.greenjangteo.user.dto.AddressDto;
import com.firstone.greenjangteo.user.form.SignUpForm;
import com.firstone.greenjangteo.user.model.Email;
import com.firstone.greenjangteo.user.model.FullName;
import com.firstone.greenjangteo.user.model.Phone;
import com.firstone.greenjangteo.user.model.Username;
import com.firstone.greenjangteo.user.model.embedment.Address;
import com.firstone.greenjangteo.user.model.embedment.Roles;
import com.firstone.greenjangteo.user.model.entity.User;
import com.firstone.greenjangteo.user.model.security.Password;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.firstone.greenjangteo.user.testutil.TestConstant.*;

public class TestObjectFactory {
    public static SignUpForm enterUserForm(String email, String username, String password, String fullName,
                                           String phone, List<String> roles) {
        return SignUpForm.builder()
                .email(email)
                .username(username)
                .password(password)
                .fullName(fullName)
                .phone(phone)
                .addressDto(AddressDto.builder()
                        .city(CITY)
                        .street(STREET)
                        .zipcode(ZIPCODE)
                        .detailedAddress(DETAILED_ADDRESS)
                        .build())
                .roles(roles)
                .build();
    }

    public static User createUser(String email, String username, String password, PasswordEncoder passwordEncoder,
                                  String fullName, String phone, List<String> roles) {
        return User.builder()
                .email(Email.of(email))
                .username(Username.of(username))
                .password(Password.from(password, passwordEncoder))
                .fullName(FullName.of(fullName))
                .phone(Phone.of(phone))
                .address(Address.from(AddressDto.builder()
                        .city(CITY)
                        .street(STREET)
                        .zipcode(ZIPCODE)
                        .detailedAddress(DETAILED_ADDRESS)
                        .build()))
                .roles(Roles.from(roles))
                .build();
    }

    public static User createUser(Long userId, String email, String username, String password,
                                  PasswordEncoder passwordEncoder, String fullName,
                                  String phone, List<String> roles) {
        return User.builder()
                .id(userId)
                .email(Email.of(email))
                .username(Username.of(username))
                .password(Password.from(password, passwordEncoder))
                .fullName(FullName.of(fullName))
                .phone(Phone.of(phone))
                .address(Address.from(AddressDto.builder()
                        .city(CITY)
                        .street(STREET)
                        .zipcode(ZIPCODE)
                        .detailedAddress(DETAILED_ADDRESS)
                        .build()))
                .roles(Roles.from(roles))
                .build();
    }
}