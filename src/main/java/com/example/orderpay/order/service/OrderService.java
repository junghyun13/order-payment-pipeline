package com.example.orderpay.order.service;


import com.example.orderpay.order.domain.*;
import com.example.orderpay.order.domain.Order;
import com.example.orderpay.order.domain.OrderItem;
import com.example.orderpay.order.domain.OrderStatus;
import com.example.orderpay.order.repository.OrderRepository;
import com.example.orderpay.order.dto.OrderRequest;
import com.example.orderpay.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse createOrder(OrderRequest req) {
        Order order = Order.builder()
                .userId(req.getUserId())
                .storeId(req.getStoreId())
                .channel(req.getChannel())
                .status(OrderStatus.PENDING)
                .totalAmount(req.calculateTotal())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .items(new ArrayList<>())
                .build();

        req.getItems().forEach(i -> order.addItem(OrderItem.builder()
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .build()));

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
