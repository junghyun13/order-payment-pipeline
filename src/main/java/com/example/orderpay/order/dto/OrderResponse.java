package com.example.orderpay.order.dto;



import com.example.orderpay.order.domain.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


@Getter @Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long storeId;
    private String status;
    private BigDecimal totalAmount;
    private String channel;
    private OffsetDateTime createdAt;
    private List<OrderItemDto> items;

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .storeId(o.getStoreId())
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount())
                .channel(o.getChannel())
                .createdAt(o.getCreatedAt())
                .items(o.getItems().stream()
                        .map(i -> new OrderItemDto(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList())
                .build();
    }

    @Getter @AllArgsConstructor
    public static class OrderItemDto {
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
