package com.example.orderpay.order.repository;

import com.example.orderpay.order.domain.Order;
import com.example.orderpay.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatus(Long storeId, OrderStatus status, Pageable pageable);
}