package com.example.orderpay.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") //메뉴주문 테이블
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long storeId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;



    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = OffsetDateTime.now();
    }
    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();

    }

}

