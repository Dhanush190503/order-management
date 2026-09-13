package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.LoginRequest;
import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.InvalidCredentialsException;
import com.ecommerce.ordermanagement.repository.RoleRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        UserRepository userRepository =
                mock(UserRepository.class);

        RoleRepository roleRepository =
                mock(RoleRepository.class);

        BCryptPasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        JwtService jwtService =
                mock(JwtService.class);

        AuthService authService =
                new AuthService(
                        userRepository,
                        roleRepository,
                        passwordEncoder,
                        jwtService
                );

        Role customerRole =
                new Role(RoleName.CUSTOMER);

        String encodedPassword =
                passwordEncoder.encode("Customer@123");

        User user =
                new User(
                        "Customer",
                        "customer@example.com",
                        encodedPassword,
                        customerRole
                );

        LoginRequest request =
                new LoginRequest();

        request.setEmail("customer@example.com");
        request.setPassword("Customer@123");

        when(userRepository.findByEmail(
                "customer@example.com"
        )).thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-token");

        String token =
                authService.login(request);

        assertEquals(
                "test-token",
                token
        );

        verify(userRepository)
                .findByEmail("customer@example.com");

        verify(jwtService)
                .generateToken(user);
    }


    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {

        UserRepository userRepository =
                mock(UserRepository.class);

        RoleRepository roleRepository =
                mock(RoleRepository.class);

        BCryptPasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        JwtService jwtService =
                mock(JwtService.class);

        AuthService authService =
                new AuthService(
                        userRepository,
                        roleRepository,
                        passwordEncoder,
                        jwtService
                );

        Role customerRole =
                new Role(RoleName.CUSTOMER);

        String encodedPassword =
                passwordEncoder.encode(
                        "CorrectPassword@123"
                );

        User user =
                new User(
                        "Customer",
                        "customer@example.com",
                        encodedPassword,
                        customerRole
                );

        LoginRequest request =
                new LoginRequest();

        request.setEmail("customer@example.com");
        request.setPassword("WrongPassword@123");

        when(userRepository.findByEmail(
                "customer@example.com"
        )).thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(any(User.class));
    }


    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {

        UserRepository userRepository =
                mock(UserRepository.class);

        RoleRepository roleRepository =
                mock(RoleRepository.class);

        BCryptPasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        JwtService jwtService =
                mock(JwtService.class);

        AuthService authService =
                new AuthService(
                        userRepository,
                        roleRepository,
                        passwordEncoder,
                        jwtService
                );

        LoginRequest request =
                new LoginRequest();

        request.setEmail("unknown@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail(
                "unknown@example.com"
        )).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
}