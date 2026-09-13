package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.AuthResponse;
import com.ecommerce.ordermanagement.dto.LoginRequest;
import com.ecommerce.ordermanagement.dto.RegisterRequest;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(
            AuthService authService,
            UserRepository userRepository
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(
                        "User registered successfully",
                        null,
                        null,
                        null
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token = authService.login(request);

        User user = userRepository.findByEmail(
                request.getEmail().trim().toLowerCase()
        ).orElseThrow();

        return ResponseEntity.ok(
                new AuthResponse(
                        "Login successful",
                        token,
                        "Bearer",
                        user.getRole().getName().name()
                )
        );
    }
}