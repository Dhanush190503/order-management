package com.ecommerce.ordermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        /*
         * JWT contains:
         *
         * "role": "ADMIN"
         * or
         * "role": "CUSTOMER"
         *
         * Spring Security converts these to:
         *
         * ROLE_ADMIN
         * ROLE_CUSTOMER
         */
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        http

                // =========================================================
                // CSRF
                // =========================================================

                .csrf(AbstractHttpConfigurer::disable)


                // =========================================================
                // SESSION MANAGEMENT
                // =========================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =========================================================
                // AUTHORIZATION
                // =========================================================

                .authorizeHttpRequests(auth -> auth

                        // =================================================
                        // AUTHENTICATION
                        // =================================================

                        /*
                         * Login and registration are public.
                         */
                        .requestMatchers("/api/auth/**")
                        .permitAll()


                        // =================================================
                        // CATEGORIES
                        // =================================================

                        /*
                         * Any authenticated user can view categories.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**"
                        )
                        .authenticated()

                        /*
                         * Only ADMIN can create categories.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Only ADMIN can update categories.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Only ADMIN can delete categories.
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categories/**"
                        )
                        .hasRole("ADMIN")


                        // =================================================
                        // PRODUCTS
                        // =================================================

                        /*
                         * Any authenticated user can view products.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**"
                        )
                        .authenticated()

                        /*
                         * Only ADMIN can create products.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Only ADMIN can update products.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Only ADMIN can delete products.
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**"
                        )
                        .hasRole("ADMIN")


                        // =================================================
                        // ORDERS
                        // =================================================

                        /*
                         * CUSTOMER and ADMIN can create orders.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        )
                        .hasAnyRole("CUSTOMER", "ADMIN")


                        /*
                         * IMPORTANT:
                         *
                         * Admin-only endpoint.
                         *
                         * This MUST come BEFORE:
                         *
                         * /api/orders/**
                         *
                         * because /api/orders/admin also matches
                         * the general /api/orders/** pattern.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/admin"
                        )
                        .hasRole("ADMIN")


                        /*
                         * Admin-only endpoint:
                         * Get orders by status.
                         *
                         * This MUST also come BEFORE the general
                         * /api/orders/** rule.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/status/**"
                        )
                        .hasRole("ADMIN")


                        /*
                         * CUSTOMER and ADMIN can access GET order endpoints.
                         *
                         * CUSTOMER access to a specific order is still
                         * checked by OrderService to make sure the order
                         * belongs to that customer.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders",
                                "/api/orders/**"
                        )
                        .hasAnyRole("CUSTOMER", "ADMIN")


                        /*
                         * Only ADMIN can update order status.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/orders/**"
                        )
                        .hasRole("ADMIN")


                        // =================================================
                        // USER MANAGEMENT
                        // =================================================

                        /*
                         * Only ADMIN can access user management APIs.
                         *
                         * CUSTOMER -> 403
                         * ADMIN    -> allowed
                         */
                        .requestMatchers(
                                "/api/users/**"
                        )
                        .hasRole("ADMIN")


                        // =================================================
                        // EVERYTHING ELSE
                        // =================================================

                        /*
                         * Any endpoint not explicitly configured above
                         * requires authentication.
                         */
                        .anyRequest()
                        .authenticated()
                )


                // =========================================================
                // JWT RESOURCE SERVER
                // =========================================================

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}