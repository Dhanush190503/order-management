package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.entity.Order;
import com.ecommerce.ordermanagement.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(
            Long userId,
            Pageable pageable
    );

    Page<Order> findByStatus(
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findByUserIdAndStatus(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );
}