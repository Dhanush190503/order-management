package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Order;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import com.ecommerce.ordermanagement.entity.RoleName;
import com.ecommerce.ordermanagement.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new IllegalStateException("CUSTOMER role not found")
                );

        user1 = new User();
        user1.setName("Order Repository User One " + System.nanoTime());
        user1.setEmail("order-repo-user1-" + System.nanoTime() + "@test.com");
        user1.setPassword("encoded-password");
        user1.setRole(customerRole);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setName("Order Repository User Two " + System.nanoTime());
        user2.setEmail("order-repo-user2-" + System.nanoTime() + "@test.com");
        user2.setPassword("encoded-password");
        user2.setRole(customerRole);
        user2 = userRepository.save(user2);
    }

    @Test
    void findByUserId_shouldReturnOrdersForUser() {
        Order order1 = new Order(
                user1,
                OrderStatus.PLACED,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        Order order2 = new Order(
                user1,
                OrderStatus.CONFIRMED,
                new BigDecimal("200.00"),
                LocalDateTime.now()
        );

        Order otherUserOrder = new Order(
                user2,
                OrderStatus.PLACED,
                new BigDecimal("300.00"),
                LocalDateTime.now()
        );

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(otherUserOrder);

        Page<Order> result = orderRepository.findByUserId(
                user1.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .hasSize(2)
                .allMatch(order ->
                        order.getUser().getId().equals(user1.getId())
                );
    }

    @Test
    void findByStatus_shouldReturnOrdersWithRequestedStatus() {
        Order placedOrder = new Order(
                user1,
                OrderStatus.PLACED,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        Order confirmedOrder = new Order(
                user2,
                OrderStatus.CONFIRMED,
                new BigDecimal("200.00"),
                LocalDateTime.now()
        );

        orderRepository.save(placedOrder);
        orderRepository.save(confirmedOrder);

        Page<Order> result = orderRepository.findByStatus(
                OrderStatus.PLACED,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(order ->
                        order.getStatus() == OrderStatus.PLACED
                );
    }

    @Test
    void findByUserIdAndStatus_shouldReturnMatchingOrders() {
        Order matchingOrder = new Order(
                user1,
                OrderStatus.SHIPPED,
                new BigDecimal("150.00"),
                LocalDateTime.now()
        );

        Order wrongStatusOrder = new Order(
                user1,
                OrderStatus.PLACED,
                new BigDecimal("250.00"),
                LocalDateTime.now()
        );

        Order wrongUserOrder = new Order(
                user2,
                OrderStatus.SHIPPED,
                new BigDecimal("350.00"),
                LocalDateTime.now()
        );

        orderRepository.save(matchingOrder);
        orderRepository.save(wrongStatusOrder);
        orderRepository.save(wrongUserOrder);

        Page<Order> result =
                orderRepository.findByUserIdAndStatus(
                        user1.getId(),
                        OrderStatus.SHIPPED,
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0).getUser().getId())
                .isEqualTo(user1.getId());

        assertThat(result.getContent().get(0).getStatus())
                .isEqualTo(OrderStatus.SHIPPED);
    }
}