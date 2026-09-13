package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import com.ecommerce.ordermanagement.service.OrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /*
     * ============================================================
     * CREATE ORDER
     * ============================================================
     *
     * POST /api/orders
     *
     * The userId comes from the JWT.
     * We do NOT accept userId from the request body.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId = getUserIdFromJwt(jwt);

        OrderResponse response =
                orderService.createOrder(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * ============================================================
     * GET MY ORDERS
     * ============================================================
     *
     * GET /api/orders
     *
     * Returns only orders belonging to the logged-in user.
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        Long userId = getUserIdFromJwt(jwt);

        Sort.Direction sortDirection;

        try {

            sortDirection =
                    Sort.Direction.fromString(direction);

        } catch (IllegalArgumentException exception) {

            sortDirection =
                    Sort.Direction.DESC;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                sortBy
                        )
                );

        return ResponseEntity.ok(
                orderService.getMyOrders(
                        userId,
                        pageable
                )
        );
    }


    /*
     * ============================================================
     * ADMIN: GET ALL ORDERS
     * ============================================================
     *
     * GET /api/orders/admin
     *
     * Returns all orders from all customers.
     *
     * SecurityConfig restricts this endpoint to ADMIN.
     *
     * Example:
     *
     * GET /api/orders/admin
     * GET /api/orders/admin?page=0&size=10
     * GET /api/orders/admin?sortBy=createdAt&direction=desc
     */
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        Sort.Direction sortDirection;

        try {

            sortDirection =
                    Sort.Direction.fromString(direction);

        } catch (IllegalArgumentException exception) {

            sortDirection =
                    Sort.Direction.DESC;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                sortBy
                        )
                );

        return ResponseEntity.ok(
                orderService.getAllOrders(
                        pageable
                )
        );
    }


    /*
     * ============================================================
     * GET MY ORDER BY ID
     * ============================================================
     *
     * GET /api/orders/{id}
     *
     * The service verifies that this order belongs
     * to the logged-in user.
     */
    @GetMapping("/{id}")
public ResponseEntity<OrderResponse> getOrderById(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
) {

    Long userId = getUserIdFromJwt(jwt);

    String role = jwt.getClaimAsString("role");

    return ResponseEntity.ok(
            orderService.getOrderById(
                    userId,
                    role,
                    id
            )
    );
}


    /*
     * ============================================================
     * ADMIN: GET ORDERS BY STATUS
     * ============================================================
     *
     * GET /api/orders/status/{status}
     *
     * SecurityConfig restricts this endpoint to ADMIN.
     *
     * Example:
     *
     * GET /api/orders/status/PLACED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        Sort.Direction sortDirection;

        try {

            sortDirection =
                    Sort.Direction.fromString(direction);

        } catch (IllegalArgumentException exception) {

            sortDirection =
                    Sort.Direction.DESC;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                sortBy
                        )
                );

        return ResponseEntity.ok(
                orderService.getOrdersByStatus(
                        status,
                        pageable
                )
        );
    }


    /*
     * ============================================================
     * ADMIN: UPDATE ORDER STATUS
     * ============================================================
     *
     * PUT /api/orders/{id}/status/{status}
     *
     * Example:
     *
     * PUT /api/orders/1/status/CONFIRMED
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable OrderStatus status
    ) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        id,
                        status
                )
        );
    }


    /*
     * ============================================================
     * JWT USER ID
     * ============================================================
     *
     * Your JWT contains:
     *
     * "userId": 2
     *
     * So we extract it here.
     */
    private Long getUserIdFromJwt(Jwt jwt) {

        if (jwt == null) {

            throw new IllegalArgumentException(
                    "Authentication token is required"
            );
        }

        Object userIdClaim =
                jwt.getClaim("userId");

        if (userIdClaim == null) {

            throw new IllegalArgumentException(
                    "User ID is missing from authentication token"
            );
        }

        if (userIdClaim instanceof Number number) {

            return number.longValue();
        }

        try {

            return Long.parseLong(
                    userIdClaim.toString()
            );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "Invalid user ID in authentication token"
            );
        }
    }
}