package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.OrderItemResponse;
import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.entity.Cart;
import com.ecommerce.ordermanagement.entity.CartItem;
import com.ecommerce.ordermanagement.entity.Order;
import com.ecommerce.ordermanagement.entity.OrderItem;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import com.ecommerce.ordermanagement.entity.Product;
import com.ecommerce.ordermanagement.entity.User;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.exception.UnauthorizedOrderAccessException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found for user"
                        )
                );

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create order because cart is empty"
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;

        List<CartItem> cartItems =
                new ArrayList<>(cart.getItems());

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found for cart item"
                );
            }

            Integer requestedQuantity =
                    cartItem.getQuantity();

            if (requestedQuantity == null ||
                    requestedQuantity <= 0) {

                throw new IllegalArgumentException(
                        "Invalid quantity for product: "
                                + product.getName()
                );
            }

            Integer availableStock =
                    product.getStockQuantity();

            if (availableStock == null ||
                    availableStock < requestedQuantity) {

                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName()
                                + ". Available stock: "
                                + availableStock
                );
            }

            BigDecimal price =
                    product.getPrice();

            BigDecimal subtotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    requestedQuantity
                            )
                    );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQuantity);
            orderItem.setPrice(price);
            orderItem.setSubtotal(subtotal);

            order.addItem(orderItem);

            totalPrice =
                    totalPrice.add(subtotal);

            product.setStockQuantity(
                    availableStock - requestedQuantity
            );

            productRepository.save(product);
        }

        order.setTotalPrice(totalPrice);

        Order savedOrder =
                orderRepository.save(order);

        cart.getItems().clear();

        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(
            Long userId,
            Pageable pageable
    ) {

        Page<Order> orders =
                orderRepository.findByUserId(
                        userId,
                        pageable
                );

        return orders.map(this::mapToResponse);
    }

    /*
     * Admin can view all orders.
     *
     * Pagination and sorting are handled
     * through the Pageable parameter.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(
            Pageable pageable
    ) {

        Page<Order> orders =
                orderRepository.findAll(pageable);

        return orders.map(this::mapToResponse);
    }

    /*
     * Get an individual order.
     *
     * ADMIN can access any order.
     *
     * CUSTOMER can access only their
     * own order.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            String role,
            Long orderId
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        if (!"ADMIN".equals(role)
                && !order.getUser()
                        .getId()
                        .equals(userId)) {

            throw new UnauthorizedOrderAccessException(
                    "You are not authorized to access this order"
            );
        }

        return mapToResponse(order);
    }

    /*
     * Backward-compatible method.
     *
     * Existing tests and internal code that use
     * the original two-argument method continue
     * to work.
     *
     * This method treats the caller as CUSTOMER,
     * so ownership is still enforced.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            Long orderId
    ) {

        return getOrderById(
                userId,
                "CUSTOMER",
                orderId
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(
            OrderStatus status,
            Pageable pageable
    ) {

        Page<Order> orders =
                orderRepository.findByStatus(
                        status,
                        pageable
                );

        return orders.map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        OrderStatus currentStatus =
                order.getStatus();

        if (!isValidStatusTransition(
                currentStatus,
                newStatus
        )) {

            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }

        /*
         * Restore stock when an order is cancelled.
         *
         * Allowed cancellation:
         *
         * PLACED -> CANCELLED
         */
        if (newStatus == OrderStatus.CANCELLED) {

            for (OrderItem orderItem : order.getItems()) {

                Product product =
                        orderItem.getProduct();

                if (product == null) {
                    throw new IllegalArgumentException(
                            "Product not found for order item"
                    );
                }

                Integer currentStock =
                        product.getStockQuantity();

                Integer orderedQuantity =
                        orderItem.getQuantity();

                if (currentStock == null) {
                    currentStock = 0;
                }

                if (orderedQuantity == null ||
                        orderedQuantity <= 0) {

                    throw new IllegalArgumentException(
                            "Invalid quantity for order item"
                    );
                }

                int newStock =
                        currentStock + orderedQuantity;

                product.setStockQuantity(newStock);

                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);

        Order updatedOrder =
                orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }

        return switch (currentStatus) {

            case PLACED ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED ->
                    false;

            case CANCELLED ->
                    false;
        };
    }

    private OrderResponse mapToResponse(
            Order order
    ) {

        List<OrderItemResponse> itemResponses =
                new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            Product product =
                    item.getProduct();

            OrderItemResponse itemResponse =
                    new OrderItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            item.getPrice(),
                            item.getQuantity(),
                            item.getSubtotal()
                    );

            itemResponses.add(itemResponse);
        }

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}