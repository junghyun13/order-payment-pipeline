package com.example.orderpay.order.event;

import com.example.orderpay.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private Long orderId;
    private OrderStatus from;
    private OrderStatus to;
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    public OrderStatusChangedEvent(Long orderId, OrderStatus from, OrderStatus to) {
        this.orderId = orderId;
        this.from = from;
        this.to = to;
        this.occurredAt = OffsetDateTime.now();
    }
}
