package com.example.orderpay.store.dto;

import java.util.List;
import lombok.Getter;
@Getter
public class StoreDto {
    private Long id;
    private String name;
    private List<MenuDto> menus;

    public StoreDto(Long id, String name, List<MenuDto> menus) {
        this.id = id;
        this.name = name;
        this.menus = menus;
    }
}
