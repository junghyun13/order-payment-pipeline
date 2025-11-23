package com.example.orderpay.store.dto;

import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class MenuDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;

    public MenuDto(Long id, String name, BigDecimal price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }
}
