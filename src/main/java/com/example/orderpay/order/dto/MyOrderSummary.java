package com.example.orderpay.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// 고객 자신의 합계 (CUSTOMER용)
@Getter
@AllArgsConstructor
public class MyOrderSummary {
    private BigDecimal totalAmount;
}
