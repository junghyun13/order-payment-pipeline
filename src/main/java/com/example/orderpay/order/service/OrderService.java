package com.example.orderpay.order.service;


import com.example.orderpay.member.User;
import com.example.orderpay.order.domain.*;
import com.example.orderpay.order.domain.Order;
import com.example.orderpay.order.domain.OrderItem;
import com.example.orderpay.order.domain.OrderStatus;
import com.example.orderpay.order.dto.*;
import com.example.orderpay.order.event.OrderCancelledEvent;
import com.example.orderpay.order.event.OrderCreatedEvent;
import com.example.orderpay.order.event.OrderStatusChangedEvent;
import com.example.orderpay.order.repository.OrderRepository;
import com.example.orderpay.common.messaging.EventPublisher;
import com.example.orderpay.search.domain.OrderSearchDocument;
import com.example.orderpay.store.domain.Menu;
import com.example.orderpay.store.repository.MenuRepository;
import com.example.orderpay.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository; // ✅ 추가

    private final ElasticsearchOperations elasticsearchOperations; // ✅ ElasticsearchOperations 추가

    public OrderResponse createOrder(User loginUser, OrderRequest req) {
        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Order order = Order.builder()
                .userId(loginUser.getId())
                .storeId(req.getStoreId())
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

        // Elasticsearch 색인 추가 (quantity 포함)
        List<OrderSearchDocument.OrderItem> searchItems = saved.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return OrderSearchDocument.OrderItem.builder()
                            .name(menu.getName())
                            .quantity(i.getQuantity())   // 수량 포함
                            .build();
                })
                .toList();

        OrderSearchDocument doc = OrderSearchDocument.builder()
                .id(String.valueOf(saved.getId())) // 여기 추가
                .orderId(saved.getId())
                .storeId(saved.getStoreId())
                .status(saved.getStatus().name())
                .totalAmount(saved.getTotalAmount().longValue())
                .items(searchItems) // 여기서 객체 리스트로 변경
                .createdAt(saved.getCreatedAt())
                .build();

        elasticsearchOperations.save(doc);


        elasticsearchOperations.indexOps(OrderSearchDocument.class).refresh();


        // 메뉴 이름 포함 DTO 생성
        List<OrderResponse.OrderItemDto> items = saved.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return new OrderResponse.OrderItemDto(
                            i.getProductId(),
                            menu.getName(),
                            i.getQuantity(),
                            i.getUnitPrice()
                    );
                })
                .toList();

        return OrderResponse.from(saved, items);
    }


    @Transactional(readOnly = true)
    public OrderResponse getOrder(User loginUser, Long id) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return new OrderResponse.OrderItemDto(
                            i.getProductId(),
                            menu.getName(),
                            i.getQuantity(),
                            i.getUnitPrice()
                    );
                }).toList();

        return OrderResponse.from(order, items);

    }

    // 주문 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(User loginUser, Long storeId, int page, int size) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders;

        if (loginUser.getRole().name().equals("CUSTOMER")) {
            orders = orderRepository.findByUserId(loginUser.getId(), pageable);
        } else if (loginUser.getRole().name().equals("OWNER")) {
            if (storeId == null)
                throw new RuntimeException("사장은 storeId로 필터링해야 합니다.");
            if (!storeRepository.existsByIdAndOwnerId(storeId, loginUser.getId()))
                throw new RuntimeException("해당 매장에 대한 권한이 없습니다.");

            orders = orderRepository.findByStoreId(storeId, pageable);
        } else {
            throw new RuntimeException("알 수 없는 유저 권한입니다.");
        }

        return orders.map(order -> {
            List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                    .map(i -> {
                        Menu menu = menuRepository.findById(i.getProductId())
                                .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                        return new OrderResponse.OrderItemDto(
                                i.getProductId(),
                                menu.getName(),
                                i.getQuantity(),
                                i.getUnitPrice()
                        );
                    }).toList();

            return OrderResponse.from(order, items);
        });
    }



    // 주문 상태 변경
    public OrderResponse changeStatus(User loginUser, Long id, OrderStatusUpdateRequest req) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        if (!loginUser.getRole().name().equals("OWNER")) {
            throw new RuntimeException("주문 상태 변경은 사장만 가능합니다.");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!storeRepository.existsByIdAndOwnerId(order.getStoreId(), loginUser.getId())) {
            throw new RuntimeException("내 매장의 주문만 상태 변경 가능합니다.");
        }

        OrderStatus from = order.getStatus();
        OrderStatus to = OrderStatus.valueOf(req.getTo());

        order.changeStatus(to);
        orderRepository.save(order);

        eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), from, to));

        // 메뉴 이름 포함 DTO 생성
        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return new OrderResponse.OrderItemDto(
                            i.getProductId(),
                            menu.getName(),
                            i.getQuantity(),
                            i.getUnitPrice()
                    );
                })
                .toList();

        return OrderResponse.from(order, items);
    }


    // 주문 취소 또는 삭제
    public OrderResponse cancelOrder(User loginUser, Long id, OrderCancelRequest req) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 고객은 본인 주문만 취소 가능
        if (loginUser.getRole().name().equals("CUSTOMER")) {
            if (!order.getUserId().equals(loginUser.getId()))
                throw new RuntimeException("본인 주문만 취소할 수 있습니다.");

            order.cancel(req.getReason());
            orderRepository.save(order);
            eventPublisher.publish(new OrderCancelledEvent(order.getId(), req.getReason()));
        }
        // 사장은 내 매장의 주문이면 삭제 가능
        else if (loginUser.getRole().name().equals("OWNER")) {
            if (!storeRepository.existsByIdAndOwnerId(order.getStoreId(), loginUser.getId())) {
                throw new RuntimeException("내 매장의 주문만 삭제할 수 있습니다.");
            }

            orderRepository.delete(order);
            eventPublisher.publish(new OrderCancelledEvent(order.getId(), "사장 삭제"));
        } else {
            throw new RuntimeException("권한이 없습니다.");
        }

        // 메뉴 이름 포함 DTO 생성
        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return new OrderResponse.OrderItemDto(
                            i.getProductId(),
                            menu.getName(),
                            i.getQuantity(),
                            i.getUnitPrice()
                    );
                })
                .toList();

        return OrderResponse.from(order, items);
    }


    public Object getOrderSummary(User loginUser, Long storeId, Long customerId) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        switch (loginUser.getRole().name()) {
            case "CUSTOMER":
                // 자신의 COMPLETED 주문 모두 조회
                List<Order> myOrders = orderRepository.findByUserIdAndStatus(loginUser.getId(), OrderStatus.COMPLETED);
                BigDecimal myTotal = myOrders.stream()
                        .flatMap(o -> o.getItems().stream())
                        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new MyOrderSummary(myTotal);

            case "OWNER":
                if (storeId == null || customerId == null)
                    throw new RuntimeException("OWNER는 storeId와 customerId가 필요합니다.");

                if (!storeRepository.existsByIdAndOwnerId(storeId, loginUser.getId()))
                    throw new RuntimeException("내 매장이 아닙니다.");

                // 특정 고객 COMPLETED 주문 조회 (시간 조건 무시)
                List<Order> orders = orderRepository.findByStoreIdAndUserIdAndStatus(
                        storeId, customerId, OrderStatus.COMPLETED
                );

                BigDecimal totalAmount = orders.stream()
                        .flatMap(o -> o.getItems().stream())
                        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new CustomerDailySummary(customerId, totalAmount);

            default:
                throw new RuntimeException("권한이 없습니다.");
        }
    }



    // 결제 완료
    public OrderResponse completePayment(User loginUser, Long orderId) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUserId().equals(loginUser.getId()))
            throw new RuntimeException("본인 주문만 결제할 수 있습니다.");

        order.changeStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), OrderStatus.PENDING, OrderStatus.COMPLETED));

        // 메뉴 이름 포함 DTO 생성
        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(i -> {
                    Menu menu = menuRepository.findById(i.getProductId())
                            .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                    return new OrderResponse.OrderItemDto(
                            i.getProductId(),
                            menu.getName(),
                            i.getQuantity(),
                            i.getUnitPrice()
                    );
                })
                .toList();

        return OrderResponse.from(order, items);
    }

    // 고객 주문 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(User loginUser, Long storeId, Long customerId, int page, int size) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");
        if (!loginUser.getRole().name().equals("OWNER"))
            throw new RuntimeException("사장만 조회할 수 있습니다.");
        if (!storeRepository.existsByIdAndOwnerId(storeId, loginUser.getId()))
            throw new RuntimeException("해당 매장에 대한 권한이 없습니다.");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders = orderRepository.findByStoreIdAndUserId(storeId, customerId, pageable);

        return orders.map(order -> {
            List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                    .map(i -> {
                        Menu menu = menuRepository.findById(i.getProductId())
                                .orElseThrow(() -> new RuntimeException("메뉴가 존재하지 않습니다: " + i.getProductId()));
                        return new OrderResponse.OrderItemDto(
                                i.getProductId(),
                                menu.getName(),
                                i.getQuantity(),
                                i.getUnitPrice()
                        );
                    })
                    .toList();
            return OrderResponse.from(order, items);
        });
    }

    public StoreDailySales getSalesByDate(User loginUser, Long storeId, LocalDate date) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        if (!loginUser.getRole().name().equals("OWNER"))
            throw new RuntimeException("권한이 없습니다.");

        if (!storeRepository.existsByIdAndOwnerId(storeId, loginUser.getId()))
            throw new RuntimeException("내 매장이 아닙니다.");

        if (date == null) date = LocalDate.now();

        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = startOfDay.plusDays(1);

        List<Order> completedOrders = orderRepository.findByStoreIdAndStatusAndCreatedAtBetween(
                storeId, OrderStatus.COMPLETED, startOfDay, endOfDay
        );

        BigDecimal totalAmount = completedOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StoreDailySales(storeId, date, totalAmount);
    }


}
