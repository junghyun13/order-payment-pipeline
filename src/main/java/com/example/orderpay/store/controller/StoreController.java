package com.example.orderpay.store.controller;

import com.example.orderpay.auth.LoginUser;
import com.example.orderpay.member.User;
import com.example.orderpay.store.domain.Menu;
import com.example.orderpay.store.domain.Store;
import com.example.orderpay.store.dto.MenuDto;
import com.example.orderpay.store.dto.StoreDto;
import com.example.orderpay.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // DTO 클래스 정의 (요청용)
    public static record StoreRequest(String storeName) {}
    public static record MenuRequest(String name, BigDecimal price, String description) {}

    // 1. 식당 등록
    @PostMapping
    public StoreDto createStore(@LoginUser User loginUser,
                                @RequestBody StoreRequest request) {
        Store store = storeService.createStore(loginUser, request.storeName());
        List<MenuDto> menuDtos = store.getMenus() == null
                ? List.of()
                : store.getMenus().stream()
                .map(m -> new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getDescription()))
                .toList();

        return new StoreDto(store.getId(), store.getName(), menuDtos);
    }

    // 2. 메뉴 등록
    @PostMapping("/{storeId}/menus")
    public MenuDto addMenu(@LoginUser User loginUser,
                           @PathVariable("storeId") Long storeId,
                           @RequestBody MenuRequest request) {
        Menu menu = storeService.addMenu(
                loginUser,
                storeId,
                request.name(),
                request.price(),
                request.description()
        );
        return new MenuDto(menu.getId(), menu.getName(), menu.getPrice(), menu.getDescription());
    }

    // 3. 메뉴 수정
    @PutMapping("/menus/{menuId}")
    public MenuDto updateMenu(@LoginUser User loginUser,
                              @PathVariable("menuId") Long menuId,
                              @RequestBody MenuRequest request) {
        Menu menu = storeService.updateMenu(
                loginUser,
                menuId,
                request.name(),
                request.price(),
                request.description()
        );
        return new MenuDto(menu.getId(), menu.getName(), menu.getPrice(), menu.getDescription());
    }

    // 4. 메뉴 삭제
    @DeleteMapping("/menus/{menuId}")
    public void deleteMenu(@LoginUser User loginUser,
                           @PathVariable("menuId") Long menuId) {
        storeService.deleteMenu(loginUser, menuId);
    }

    // 5. 메뉴 조회
    @GetMapping("/{storeId}/menus")
    public List<MenuDto> listMenus(@PathVariable("storeId") Long storeId) {
        return storeService.listMenus(storeId).stream()
                .map(m -> new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getDescription()))
                .toList();
    }

    @GetMapping("/my-stores")
    public List<StoreDto> getMyStores(@LoginUser User loginUser) {
        return storeService.getMyStores(loginUser).stream()
                .map(store -> {
                    List<MenuDto> menuDtos = store.getMenus().stream()
                            .map(m -> new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getDescription()))
                            .toList();
                    return new StoreDto(store.getId(), store.getName(), menuDtos);
                })
                .toList();
    }

    // 식당 삭제
    @DeleteMapping("/{storeId}")
    public void deleteStore(@LoginUser User loginUser,
                            @PathVariable("storeId") Long storeId) {
        storeService.deleteStore(loginUser, storeId);
    }



    //단일 메뉴조회
    @GetMapping("/menus/{menuId}")
    public MenuDto getMenu(@PathVariable("menuId") Long menuId) {
        Menu menu = storeService.getMenu(menuId);
        return new MenuDto(menu.getId(), menu.getName(), menu.getPrice(), menu.getDescription());
    }

    // 로그인 사용자 기준 식당 목록 조회
    @GetMapping
    public List<StoreDto> getStores(@LoginUser User loginUser) {
        List<Store> stores = storeService.getStoresByRole(loginUser);

        return stores.stream()
                .map(store -> new StoreDto(
                        store.getId(),
                        store.getName(),
                        store.getMenus() == null
                                ? List.of()
                                : store.getMenus().stream()
                                .map(m -> new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getDescription()))
                                .toList()
                ))
                .toList();
    }


}
