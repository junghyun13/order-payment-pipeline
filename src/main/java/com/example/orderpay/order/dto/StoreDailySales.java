package com.example.orderpay.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StoreDailySales {
    private Long storeId;
    private LocalDate date;
    private BigDecimal totalAmount;
}