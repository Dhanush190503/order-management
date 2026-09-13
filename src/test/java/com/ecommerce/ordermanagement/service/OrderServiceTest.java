package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.entity.Cart;
import com.ecommerce.ordermanagement.entity.CartItem;
import com.ecommerce.ordermanagement.entity.Category;
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

import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void getOrderById_shouldReturnOrder_whenOrderBelongsToUser() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);
        when(user.getId()).thenReturn(3L);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response =
                orderService.getOrderById(3L, 1L);

        assertEquals(OrderStatus.PLACED, response.getStatus());
        assertEquals(
                new BigDecimal("799.99"),
                response.getTotalPrice()
        );
        assertEquals(1, response.getItems().size());
        assertEquals(
                "Samsung Galaxy S24",
                response.getItems().get(0).getProductName()
        );
        assertEquals(
                "S24-001",
                response.getItems().get(0).getSku()
        );
        assertEquals(
                1,
                response.getItems().get(0).getQuantity()
        );

        verify(orderRepository).findById(1L);
        verify(user).getId();
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderDoesNotExist() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(3L, 999L)
        );

        verify(orderRepository).findById(999L);
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderBelongsToAnotherUser() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User orderOwner = mock(User.class);
        when(orderOwner.getId()).thenReturn(5L);

        Order order = new Order(
                orderOwner,
                OrderStatus.PLACED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                UnauthorizedOrderAccessException.class,
                () -> orderService.getOrderById(3L, 1L)
        );

        verify(orderRepository).findById(1L);
        verify(orderOwner).getId();
    }

    @Test
    void getMyOrders_shouldReturnOrdersForUser() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByUserId(3L, pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> response =
                orderService.getMyOrders(3L, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(
                OrderStatus.PLACED,
                response.getContent().get(0).getStatus()
        );
        assertEquals(
                new BigDecimal("799.99"),
                response.getContent().get(0).getTotalPrice()
        );
        assertEquals(
                "Samsung Galaxy S24",
                response.getContent()
                        .get(0)
                        .getItems()
                        .get(0)
                        .getProductName()
        );

        verify(orderRepository)
                .findByUserId(3L, pageable);
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findAll(pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> response =
                orderService.getAllOrders(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(
                OrderStatus.CONFIRMED,
                response.getContent().get(0).getStatus()
        );
        assertEquals(
                new BigDecimal("799.99"),
                response.getContent().get(0).getTotalPrice()
        );
        assertEquals(
                1,
                response.getContent().get(0).getItems().size()
        );
        assertEquals(
                "Samsung Galaxy S24",
                response.getContent()
                        .get(0)
                        .getItems()
                        .get(0)
                        .getProductName()
        );

        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getOrdersByStatus_shouldReturnOrdersWithRequestedStatus() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.SHIPPED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByStatus(
                OrderStatus.SHIPPED,
                pageable
        )).thenReturn(orderPage);

        Page<OrderResponse> response =
                orderService.getOrdersByStatus(
                        OrderStatus.SHIPPED,
                        pageable
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(
                OrderStatus.SHIPPED,
                response.getContent().get(0).getStatus()
        );
        assertEquals(
                new BigDecimal("799.99"),
                response.getContent().get(0).getTotalPrice()
        );
        assertEquals(
                1,
                response.getContent().get(0).getItems().size()
        );

        verify(orderRepository).findByStatus(
                OrderStatus.SHIPPED,
                pageable
        );
    }

    @Test
    void updateOrderStatus_shouldUpdateStatus_whenTransitionIsValid() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CONFIRMED
                );

        assertEquals(
                OrderStatus.CONFIRMED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("799.99"),
                response.getTotalPrice()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldThrowException_whenTransitionIsInvalid() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Order order = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.SHIPPED
                )
        );

        assertEquals(
                OrderStatus.PLACED,
                order.getStatus()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateOrderStatus_shouldCancelOrderAndRestoreStock() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                5,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("1599.98"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                2,
                new BigDecimal("799.99"),
                new BigDecimal("1599.98")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CANCELLED
                );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.CANCELLED,
                response.getStatus()
        );

        assertEquals(
                7,
                product.getStockQuantity()
        );

        verify(orderRepository).findById(1L);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldChangeConfirmedToShipped() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.SHIPPED
                );

        assertEquals(
                OrderStatus.SHIPPED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.SHIPPED,
                response.getStatus()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(productRepository, never()).save(product);
    }

    @Test
    void updateOrderStatus_shouldChangeShippedToDelivered() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.SHIPPED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                1,
                new BigDecimal("799.99"),
                new BigDecimal("799.99")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.updateOrderStatus(
                        1L,
                        OrderStatus.DELIVERED
                );

        assertEquals(
                OrderStatus.DELIVERED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.DELIVERED,
                response.getStatus()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(productRepository, never()).save(product);
    }

    @Test
    void updateOrderStatus_shouldRejectTransitionFromDelivered() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Order order = new Order(
                user,
                OrderStatus.DELIVERED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CANCELLED
                )
        );

        assertEquals(
                OrderStatus.DELIVERED,
                order.getStatus()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateOrderStatus_shouldRejectTransitionFromCancelled() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Order order = new Order(
                user,
                OrderStatus.CANCELLED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CONFIRMED
                )
        );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateOrderStatus_shouldRejectCancellationFromConfirmed() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                5,
                "S24-001",
                category
        );

        Order order = new Order(
                user,
                OrderStatus.CONFIRMED,
                new BigDecimal("799.99"),
                LocalDateTime.now()
        );

        OrderItem orderItem = new OrderItem(
                product,
                2,
                new BigDecimal("799.99"),
                new BigDecimal("1599.98")
        );

        order.addItem(orderItem);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        1L,
                        OrderStatus.CANCELLED
                )
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                order.getStatus()
        );

        assertEquals(
                5,
                product.getStockQuantity()
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(order);
        verify(productRepository, never()).save(product);
    }

    @Test
    void updateOrderStatus_shouldThrowException_whenOrderDoesNotExist() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(
                        999L,
                        OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );
    }

    @Test
    void createOrder_shouldCreateOrderAndDeductStock() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);
        when(user.getId()).thenReturn(3L);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                10,
                "S24-001",
                category
        );

        CartItem cartItem = mock(CartItem.class);

        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(2);

        Cart cart = mock(Cart.class);

        when(cart.getItems())
                .thenReturn(new ArrayList<>(List.of(cartItem)));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(3L))
                .thenReturn(Optional.of(cart));

        Order savedOrder = new Order(
                user,
                OrderStatus.PLACED,
                new BigDecimal("1599.98"),
                LocalDateTime.now()
        );

        when(orderRepository.save(
                org.mockito.ArgumentMatchers.any(Order.class)
        )).thenReturn(savedOrder);

        OrderResponse response =
                orderService.createOrder(3L);

        assertEquals(
                OrderStatus.PLACED,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("1599.98"),
                response.getTotalPrice()
        );

        assertEquals(
                8,
                product.getStockQuantity()
        );

        verify(userRepository).findById(3L);
        verify(cartRepository).findByUserId(3L);
        verify(productRepository).save(product);
        verify(orderRepository).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrder_shouldThrowException_whenCartIsEmpty() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Cart cart = mock(Cart.class);

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(3L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(new ArrayList<>());

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(3L)
        );

        verify(userRepository).findById(3L);
        verify(cartRepository).findByUserId(3L);

        verify(orderRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );

        verify(productRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Product.class)
        );
    }

    @Test
    void createOrder_shouldThrowException_whenStockIsInsufficient() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        Category category = new Category(
                "Electronics",
                "Electronic products"
        );

        Product product = new Product(
                "Samsung Galaxy S24",
                "Smartphone",
                new BigDecimal("799.99"),
                1,
                "S24-001",
                category
        );

        CartItem cartItem = mock(CartItem.class);

        when(cartItem.getProduct())
                .thenReturn(product);

        when(cartItem.getQuantity())
                .thenReturn(2);

        Cart cart = mock(Cart.class);

        when(cart.getItems())
                .thenReturn(new ArrayList<>(List.of(cartItem)));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(3L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(3L)
        );

        assertEquals(
                1,
                product.getStockQuantity()
        );

        verify(userRepository).findById(3L);
        verify(cartRepository).findByUserId(3L);

        verify(productRepository, never()).save(product);

        verify(orderRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );

        verify(cartRepository, never()).save(cart);
    }

    @Test
    void createOrder_shouldThrowException_whenCartDoesNotExist() {

        OrderRepository orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        OrderService orderService = new OrderService(
                orderRepository,
                cartRepository,
                productRepository,
                userRepository
        );

        User user = mock(User.class);

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(3L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(3L)
        );

        verify(userRepository).findById(3L);
        verify(cartRepository).findByUserId(3L);

        verify(orderRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );
    }
}