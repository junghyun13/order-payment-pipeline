package com.example.orderpay.search.controller;

import com.example.orderpay.auth.LoginUser;
import com.example.orderpay.member.User;
import com.example.orderpay.search.dto.SearchResponse;
import com.example.orderpay.search.service.SearchService;
import com.example.orderpay.store.domain.Menu;
import com.example.orderpay.store.dto.MenuDto;
import com.example.orderpay.store.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public SearchResponse searchOrders(
            @LoginUser User loginUser,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return searchService.search(loginUser, q, storeId, status);
    }


    private final MenuRepository menuRepository;

    // SearchController.java
    @GetMapping("/searchforkeyword")
    public List<MenuDto> searchMenus(
            @LoginUser User loginUser,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "storeId", required = false) Long storeId) {

        if (storeId == null) {
            throw new RuntimeException("storeId가 필요합니다.");
        }

        List<Menu> menus;
        if (q == null || q.isEmpty()) {
            menus = menuRepository.findByStoreId(storeId);
        } else {
            menus = menuRepository.findByStoreIdAndNameContainingIgnoreCase(storeId, q);
        }

        return menus.stream()
                .map(menu -> new MenuDto(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getDescription()
                ))
                .toList();
    }




}