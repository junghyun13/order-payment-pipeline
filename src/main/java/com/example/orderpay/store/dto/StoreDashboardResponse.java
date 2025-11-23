package com.example.orderpay.store.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreDashboardResponse {
    private Long storeId;
    private Map<String, Long> counts; // status -> count
    private Metrics metrics;
    private List<Long> recentOrderIds;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Metrics {
        private Long totalOrders;
        private BigDecimal totalAmount;
    }
}
