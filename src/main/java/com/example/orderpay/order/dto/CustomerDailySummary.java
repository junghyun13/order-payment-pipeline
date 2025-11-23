package com.example.orderpay.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// 고객 단건 결재 합계 (OWNER용)
@Getter
@AllArgsConstructor
public class CustomerDailySummary {
    private Long customerId;
    private BigDecimal totalAmount;
}
