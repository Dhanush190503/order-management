package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.LoginRequest;
import com.ecommerce.ordermanagement.dto.RegisterRequest;
import com.ecommerce.ordermanagement.entity.Role;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.InvalidCredentialsException;
import com.ecommerce.ordermanagement.repository.RoleRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new IllegalStateException("CUSTOMER role not found"));

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(customerRole);

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtService.generateToken(user);
    }
}