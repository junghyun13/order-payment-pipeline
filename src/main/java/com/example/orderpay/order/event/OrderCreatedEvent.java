package com.example.orderpay.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long storeId;
    private BigDecimal totalAmount;
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    public OrderCreatedEvent(Long orderId, Long storeId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.occurredAt = OffsetDateTime.now();
    }
}