package com.example.orderpay.order.service;


import com.example.orderpay.order.domain.*;
import com.example.orderpay.order.domain.Order;
import com.example.orderpay.order.domain.OrderItem;
import com.example.orderpay.order.domain.OrderStatus;
import com.example.orderpay.order.dto.OrderCancelRequest;
import com.example.orderpay.order.dto.OrderStatusUpdateRequest;
import com.example.orderpay.order.event.OrderCancelledEvent;
import com.example.orderpay.order.event.OrderCreatedEvent;
import com.example.orderpay.order.event.OrderStatusChangedEvent;
import com.example.orderpay.order.repository.OrderRepository;
import com.example.orderpay.order.dto.OrderRequest;
import com.example.orderpay.order.dto.OrderResponse;
import com.example.orderpay.common.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

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

        // ✅ Redpanda 이벤트 발행 추가
        // Redpanda 이벤트 발행
        eventPublisher.publish(
                "order-events",
                new OrderCreatedEvent(
                        saved.getId(),
                        saved.getStoreId(),
                        saved.getTotalAmount(),
                        saved.getCreatedAt()
                )
        );

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {// 주문 단건 조회
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    // 주문 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Long storeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders;
        if (storeId != null && status != null) {
            orders = orderRepository.findByStoreIdAndStatus(storeId, OrderStatus.valueOf(status), pageable);
        } else if (storeId != null) {
            orders = orderRepository.findByStoreId(storeId, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(OrderResponse::from);
    }

    // 주문 상태 변경
    public OrderResponse changeStatus(Long id, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderStatus from = order.getStatus();
        OrderStatus to = OrderStatus.valueOf(req.getTo());
        order.changeStatus(to);

        orderRepository.save(order);
        eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), from, to));

        return OrderResponse.from(order);
    }

    // 주문 취소
    public OrderResponse cancelOrder(Long id, OrderCancelRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.cancel(req.getReason());
        orderRepository.save(order);

        eventPublisher.publish(new OrderCancelledEvent(order.getId(), req.getReason()));

        return OrderResponse.from(order);

    }
}
