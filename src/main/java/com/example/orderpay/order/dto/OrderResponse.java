package com.example.orderpay.order.dto;



import com.example.orderpay.order.domain.Order;
import com.example.orderpay.store.domain.Menu;

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
    private OffsetDateTime createdAt;
    private List<OrderItemDto> items;

    // 메뉴 이름을 서비스에서 전달받도록 수정
    public static OrderResponse from(Order o, List<OrderItemDto> items) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .storeId(o.getStoreId())
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt())
                .items(items)
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long productId;
        private String name; // 메뉴 이름 포함
        private int quantity;
        private BigDecimal unitPrice;
    }
}
