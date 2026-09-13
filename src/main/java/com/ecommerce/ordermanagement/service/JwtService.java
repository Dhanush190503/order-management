package com.ecommerce.ordermanagement.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ecommerce.ordermanagement.entity.User;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {

        Instant now = Instant.now();

        String role = user.getRole().getName().name();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ecommerce-order-management")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.MILLIS))
                .claim("userId", user.getId())
                .claim("name", user.getName())
                .claim("role", role)
                .build();

        String token = jwtEncoder
        .encode(JwtEncoderParameters.from(claims))
        .getTokenValue();

return token;
    }
}