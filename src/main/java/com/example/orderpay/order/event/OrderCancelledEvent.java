package com.example.orderpay.order.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    private Long orderId;
    private String reason;
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    public OrderCancelledEvent(Long orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
        this.occurredAt = OffsetDateTime.now();
    }
}
