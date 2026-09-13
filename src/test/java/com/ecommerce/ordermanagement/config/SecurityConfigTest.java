package com.ecommerce.ordermanagement.config;

import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.dto.UserResponse;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import com.ecommerce.ordermanagement.service.AuthService;
import com.ecommerce.ordermanagement.service.OrderService;
import com.ecommerce.ordermanagement.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.PageImpl;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;


    /*
     * ============================================================
     * MOCK SERVICES
     * ============================================================
     *
     * These tests are testing SecurityConfig.
     *
     * We therefore mock the business services so that the tests
     * do not depend on real order/user database data.
     */

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;


    /*
     * ============================================================
     * TEST DATA
     * ============================================================
     */

    @BeforeEach
    void setUp() {

        /*
         * GET /api/orders
         */
        when(orderService.getMyOrders(
                any(Long.class),
                any()
        )).thenReturn(
                new PageImpl<>(
                        List.<OrderResponse>of()
                )
        );


        /*
         * GET /api/orders/admin
         */
        when(orderService.getAllOrders(
                any()
        )).thenReturn(
                new PageImpl<>(
                        List.<OrderResponse>of()
                )
        );


        /*
         * GET /api/orders/status/{status}
         */
        when(orderService.getOrdersByStatus(
                any(OrderStatus.class),
                any()
        )).thenReturn(
                new PageImpl<>(
                        List.<OrderResponse>of()
                )
        );


        /*
         * POST /api/orders
         */
        when(orderService.createOrder(
                any(Long.class)
        )).thenReturn(
                new OrderResponse(
                        1L,
                        OrderStatus.PLACED,
                        BigDecimal.ZERO,
                        null,
                        List.of()
                )
        );


        /*
         * PUT /api/orders/{id}/status/{status}
         */
        when(orderService.updateOrderStatus(
                any(Long.class),
                any(OrderStatus.class)
        )).thenReturn(
                new OrderResponse(
                        1L,
                        OrderStatus.CONFIRMED,
                        BigDecimal.ZERO,
                        null,
                        List.of()
                )
        );


        /*
         * GET /api/users
         */
        when(userService.getAllUsers(
                any(),
                any()
        )).thenReturn(
                new PageImpl<>(
                        List.<UserResponse>of()
                )
        );
    }


    // ============================================================
    // TEST 50
    // No JWT -> 401
    // ============================================================

    @Test
    void protectedEndpoint_shouldReturn401_whenJwtIsMissing()
            throws Exception {

        mockMvc.perform(
                get("/api/orders")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    // ============================================================
    // TEST 51
    // CUSTOMER -> admin orders -> 403
    // ============================================================

    @Test
    void adminOrders_shouldReturn403_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                get("/api/orders/admin")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }


    // ============================================================
    // TEST 52
    // ADMIN -> admin orders -> 200
    // ============================================================

    @Test
    void adminOrders_shouldReturn200_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                get("/api/orders/admin")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // TEST 53
    // CUSTOMER -> orders by status -> 403
    // ============================================================

    @Test
    void ordersByStatus_shouldReturn403_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                get("/api/orders/status/PLACED")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }


    // ============================================================
    // TEST 54
    // ADMIN -> orders by status -> 200
    // ============================================================

    @Test
    void ordersByStatus_shouldReturn200_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                get("/api/orders/status/PLACED")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // TEST 55
    // CUSTOMER -> update order -> 403
    // ============================================================

    @Test
    void updateOrderStatus_shouldReturn403_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                put("/api/orders/1/status/CONFIRMED")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }


    // ============================================================
    // TEST 56
    // ADMIN -> update order -> 200
    // ============================================================

    @Test
    void updateOrderStatus_shouldReturn200_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                put("/api/orders/1/status/CONFIRMED")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // TEST 57
    // CUSTOMER -> users -> 403
    // ============================================================

    @Test
    void usersEndpoint_shouldReturn403_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }


    // ============================================================
    // TEST 58
    // ADMIN -> users -> 200
    // ============================================================

    @Test
    void usersEndpoint_shouldReturn200_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // TEST 59
    // CUSTOMER -> create order -> 201
    // ============================================================

    @Test
    void createOrder_shouldReturn201_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                post("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isCreated()
        );
    }


    // ============================================================
    // TEST 60
    // ADMIN -> create order -> 201
    // ============================================================

    @Test
    void createOrder_shouldReturn201_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                post("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isCreated()
        );
    }


    // ============================================================
    // TEST 61
    // Auth endpoint is public
    // ============================================================

    @Test
    void authEndpoint_shouldNotRequireAuthentication()
            throws Exception {

        /*
         * The endpoint is public, so Spring Security should not
         * return 401 or 403.
         *
         * The empty request body is invalid, so the controller
         * is expected to return 400.
         */
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                "application/json"
                        )
                        .content("{}")
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    // ============================================================
    // TEST 62
    // Invalid JWT -> 401
    // ============================================================

    @Test
    void protectedEndpoint_shouldReturn401_whenJwtIsInvalid()
            throws Exception {

        mockMvc.perform(
                get("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    // ============================================================
    // TEST 63
    // CUSTOMER -> GET /api/orders -> 200
    // ============================================================

    @Test
    void getMyOrders_shouldReturn200_whenCustomerAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "customer@example.com",
                        3L,
                        "CUSTOMER"
                );

        mockMvc.perform(
                get("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // TEST 64
    // ADMIN -> GET /api/orders -> 200
    // ============================================================

    @Test
    void getMyOrders_shouldReturn200_whenAdminAccessesEndpoint()
            throws Exception {

        String token =
                createToken(
                        "admin@ecommerce.com",
                        2L,
                        "ADMIN"
                );

        mockMvc.perform(
                get("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    // ============================================================
    // JWT CREATION
    // ============================================================

    private String createToken(
            String email,
            Long userId,
            String role
    ) {

        Instant now =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                "ecommerce-order-management"
                        )
                        .subject(email)
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(3600)
                        )
                        .claim(
                                "userId",
                                userId
                        )
                        .claim(
                                "name",
                                "Test User"
                        )
                        .claim(
                                "role",
                                role
                        )
                        .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                claims
                        )
                )
                .getTokenValue();
    }
}