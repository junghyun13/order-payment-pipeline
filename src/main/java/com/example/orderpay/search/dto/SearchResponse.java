package com.example.orderpay.search.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {

    private long tookMs;
    private List<OrderHit> hits;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderHit {
        private Long orderId;
        private Long storeId;
        private String status;
        private Long totalAmount;
        private List<OrderItem> items; // 객체로 변경
        private OffsetDateTime createdAt;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OrderItem {
            private String name;     // 메뉴 이름
            private int quantity;    // 수량
        }
    }
}