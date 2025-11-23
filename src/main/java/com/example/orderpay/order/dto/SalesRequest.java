package com.example.orderpay.order.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalesRequest {
    private LocalDate date; // 조회할 날짜
}