package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.AuthResponse;
import com.ecommerce.ordermanagement.dto.LoginRequest;
import com.ecommerce.ordermanagement.dto.RegisterRequest;
import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.InvalidCredentialsException;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    // ============================================================
    // TEST 30
    // Invalid login -> InvalidCredentialsException
    // ============================================================

    @Test
    void login_shouldThrowInvalidCredentialsException_whenCredentialsAreInvalid() {

        AuthService authService =
                mock(AuthService.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        AuthController authController =
                new AuthController(
                        authService,
                        userRepository
                );

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "wrong@example.com"
        );

        request.setPassword(
                "WrongPassword"
        );

        when(authService.login(request))
                .thenThrow(
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authController.login(request)
        );

        verify(authService)
                .login(request);

        verifyNoInteractions(
                userRepository
        );
    }


    // ============================================================
    // TEST 31
    // Login succeeds in AuthService but user lookup fails
    // ============================================================

    @Test
    void login_shouldThrowException_whenUserLookupFailsAfterAuthentication() {

        AuthService authService =
                mock(AuthService.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        AuthController authController =
                new AuthController(
                        authService,
                        userRepository
                );

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "customer@example.com"
        );

        request.setPassword(
                "Customer@123"
        );

        /*
         * AuthService.login() succeeds and returns a token.
         */
        when(authService.login(request))
                .thenReturn("test-token");

        /*
         * Controller then tries to find the user again
         * in order to get the role.
         */
        when(userRepository.findByEmail(
                "customer@example.com"
        )).thenReturn(
                Optional.empty()
        );

        /*
         * AuthController currently uses .orElseThrow()
         * without specifying an exception.
         *
         * Therefore Java throws NoSuchElementException.
         */
        assertThrows(
                java.util.NoSuchElementException.class,
                () -> authController.login(request)
        );

        verify(authService)
                .login(request);

        verify(userRepository)
                .findByEmail(
                        "customer@example.com"
                );
    }


    // ============================================================
    // TEST 32
    // Registration delegates correctly to AuthService
    // ============================================================

    @Test
    void register_shouldDelegateRequestToAuthService() {

        AuthService authService =
                mock(AuthService.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        AuthController authController =
                new AuthController(
                        authService,
                        userRepository
                );

        RegisterRequest request =
                new RegisterRequest();

        request.setName(
                "Test Customer"
        );

        request.setEmail(
                "testcustomer@example.com"
        );

        request.setPassword(
                "Test@12345"
        );

        ResponseEntity<AuthResponse> response =
                authController.register(request);

        /*
         * Controller should return 201 CREATED.
         */
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        /*
         * Verify response message.
         */
        assertEquals(
                "User registered successfully",
                response.getBody().getMessage()
        );

        /*
         * Registration does not return a JWT.
         */
        assertNull(
                response.getBody().getToken()
        );

        assertNull(
                response.getBody().getTokenType()
        );

        assertNull(
                response.getBody().getRole()
        );

        /*
         * Most important part of this test:
         * verify that AuthController delegated
         * registration to AuthService.
         */
        verify(authService)
                .register(request);

        /*
         * Controller should not directly access UserRepository
         * during registration.
         */
        verifyNoInteractions(
                userRepository
        );
    }
}