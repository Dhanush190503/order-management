package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import com.ecommerce.ordermanagement.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    // ============================================================
    // TEST 39
    // Create order -> 201 CREATED
    // ============================================================

    @Test
    void createOrder_shouldReturn201_whenOrderIsCreated() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt = createJwtWithUserId(3L);

        OrderResponse orderResponse =
                new OrderResponse(
                        1L,
                        OrderStatus.PLACED,
                        new BigDecimal("100.00"),
                        null,
                        List.of()
                );

        when(orderService.createOrder(3L))
                .thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response =
                orderController.createOrder(jwt);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                orderResponse,
                response.getBody()
        );

        assertEquals(
                1L,
                response.getBody().getOrderId()
        );

        assertEquals(
                OrderStatus.PLACED,
                response.getBody().getStatus()
        );

        assertEquals(
                new BigDecimal("100.00"),
                response.getBody().getTotalPrice()
        );

        verify(orderService)
                .createOrder(3L);

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 40
    // Get my orders -> 200 OK
    // ============================================================

    @Test
    void getMyOrders_shouldReturn200_whenOrdersExist() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt = createJwtWithUserId(3L);

        OrderResponse orderResponse =
                new OrderResponse(
                        1L,
                        OrderStatus.PLACED,
                        new BigDecimal("100.00"),
                        null,
                        List.of()
                );

        Page<OrderResponse> page =
                new PageImpl<>(
                        List.of(orderResponse),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        ),
                        1
                );

        when(orderService.getMyOrders(
                3L,
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response =
                orderController.getMyOrders(
                        jwt,
                        0,
                        10,
                        "createdAt",
                        "desc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        assertEquals(
                1,
                response.getBody().getTotalElements()
        );

        assertEquals(
                1L,
                response.getBody()
                        .getContent()
                        .get(0)
                        .getOrderId()
        );

        verify(orderService)
                .getMyOrders(
                        3L,
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 41
    // Get order by ID -> 200 OK
    // ============================================================

    @Test
    void getOrderById_shouldReturn200_whenOrderExists() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt = createJwtWithUserId(3L);

        OrderResponse orderResponse =
                new OrderResponse(
                        10L,
                        OrderStatus.CONFIRMED,
                        new BigDecimal("250.00"),
                        null,
                        List.of()
                );

        when(orderService.getOrderById(
                3L,
                "CUSTOMER",
                10L
        )).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response =
                orderController.getOrderById(
                        jwt,
                        10L
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                orderResponse,
                response.getBody()
        );

        assertEquals(
                10L,
                response.getBody().getOrderId()
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getBody().getStatus()
        );

        verify(orderService)
                .getOrderById(
                        3L,
                        "CUSTOMER",
                        10L
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 42
    // Unauthorized order access
    // ============================================================

    @Test
    void getOrderById_shouldThrowException_whenOrderBelongsToAnotherUser() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt = createJwtWithUserId(3L);

        when(orderService.getOrderById(
                3L,
                "CUSTOMER",
                10L
        )).thenThrow(
                new com.ecommerce.ordermanagement.exception.UnauthorizedOrderAccessException(
                        "You are not authorized to access this order"
                )
        );

        assertThrows(
                com.ecommerce.ordermanagement.exception.UnauthorizedOrderAccessException.class,
                () -> orderController.getOrderById(
                        jwt,
                        10L
                )
        );

        verify(orderService)
                .getOrderById(
                        3L,
                        "CUSTOMER",
                        10L
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 43
    // Admin get all orders -> 200 OK
    // ============================================================

    @Test
    void getAllOrders_shouldReturn200_whenOrdersExist() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        OrderResponse orderResponse =
                new OrderResponse(
                        1L,
                        OrderStatus.PLACED,
                        new BigDecimal("150.00"),
                        null,
                        List.of()
                );

        Page<OrderResponse> page =
                new PageImpl<>(
                        List.of(orderResponse),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        ),
                        1
                );

        when(orderService.getAllOrders(
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response =
                orderController.getAllOrders(
                        0,
                        10,
                        "createdAt",
                        "desc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        assertEquals(
                1,
                response.getBody().getTotalElements()
        );

        verify(orderService)
                .getAllOrders(
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 44
    // Get orders by status -> 200 OK
    // ============================================================

    @Test
    void getOrdersByStatus_shouldReturn200_whenOrdersExist() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        OrderResponse orderResponse =
                new OrderResponse(
                        5L,
                        OrderStatus.CONFIRMED,
                        new BigDecimal("300.00"),
                        null,
                        List.of()
                );

        Page<OrderResponse> page =
                new PageImpl<>(
                        List.of(orderResponse),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        ),
                        1
                );

        when(orderService.getOrdersByStatus(
                OrderStatus.CONFIRMED,
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response =
                orderController.getOrdersByStatus(
                        OrderStatus.CONFIRMED,
                        0,
                        10,
                        "createdAt",
                        "desc"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getBody()
                        .getContent()
                        .get(0)
                        .getStatus()
        );

        verify(orderService)
                .getOrdersByStatus(
                        OrderStatus.CONFIRMED,
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 45
    // Update order status -> 200 OK
    // ============================================================

    @Test
    void updateOrderStatus_shouldReturn200_whenStatusIsUpdated() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        OrderResponse orderResponse =
                new OrderResponse(
                        10L,
                        OrderStatus.CONFIRMED,
                        new BigDecimal("200.00"),
                        null,
                        List.of()
                );

        when(orderService.updateOrderStatus(
                10L,
                OrderStatus.CONFIRMED
        )).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response =
                orderController.updateOrderStatus(
                        10L,
                        OrderStatus.CONFIRMED
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                orderResponse,
                response.getBody()
        );

        assertEquals(
                10L,
                response.getBody().getOrderId()
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getBody().getStatus()
        );

        verify(orderService)
                .updateOrderStatus(
                        10L,
                        OrderStatus.CONFIRMED
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 46
    // Null JWT -> authentication token required
    // ============================================================

    @Test
    void createOrder_shouldThrowException_whenJwtIsNull() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderController.createOrder(null)
                );

        assertEquals(
                "Authentication token is required",
                exception.getMessage()
        );
    }


    // ============================================================
    // TEST 47
    // JWT without userId -> exception
    // ============================================================

    @Test
    void createOrder_shouldThrowException_whenUserIdIsMissingFromJwt() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .claim("sub", "customer@example.com")
                        .issuedAt(Instant.now())
                        .expiresAt(
                                Instant.now().plusSeconds(3600)
                        )
                        .build();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderController.createOrder(jwt)
                );

        assertEquals(
                "User ID is missing from authentication token",
                exception.getMessage()
        );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 48
    // Invalid userId in JWT -> exception
    // ============================================================

    @Test
    void createOrder_shouldThrowException_whenUserIdInJwtIsInvalid() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .claim("sub", "customer@example.com")
                        .claim("userId", "invalid-id")
                        .issuedAt(Instant.now())
                        .expiresAt(
                                Instant.now().plusSeconds(3600)
                        )
                        .build();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderController.createOrder(jwt)
                );

        assertEquals(
                "Invalid user ID in authentication token",
                exception.getMessage()
        );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // TEST 49
    // Invalid direction falls back to DESC
    // ============================================================

    @Test
    void getMyOrders_shouldUseDescendingSort_whenDirectionIsInvalid() {

        OrderService orderService =
                mock(OrderService.class);

        OrderController orderController =
                new OrderController(orderService);

        Jwt jwt =
                createJwtWithUserId(3L);

        Page<OrderResponse> page =
                new PageImpl<>(
                        List.of()
                );

        when(orderService.getMyOrders(
                3L,
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
        )).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response =
                orderController.getMyOrders(
                        jwt,
                        0,
                        10,
                        "createdAt",
                        "invalid"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                page,
                response.getBody()
        );

        verify(orderService)
                .getMyOrders(
                        3L,
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                );

        verifyNoMoreInteractions(
                orderService
        );
    }


    // ============================================================
    // HELPER
    // ============================================================

    private Jwt createJwtWithUserId(Long userId) {

        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("sub", "customer@example.com")
                .claim("userId", userId)
                .claim("role", "CUSTOMER")
                .issuedAt(Instant.now())
                .expiresAt(
                        Instant.now().plusSeconds(3600)
                )
                .build();
    }
}