package com.example.orderpay.store.service;

import com.example.orderpay.member.User;
import com.example.orderpay.store.domain.Menu;
import com.example.orderpay.store.domain.Store;
import com.example.orderpay.store.repository.MenuRepository;
import com.example.orderpay.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    // 식당 등록 (OWNER만)
    public Store createStore(User loginUser, String storeName) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");
        if (!loginUser.getRole().name().equals("OWNER")) throw new RuntimeException("식당 주인만 등록 가능합니다.");

        Store store = Store.builder()
                .name(storeName)
                .owner(loginUser)
                .build();

        return storeRepository.save(store);
    }

    // 메뉴 등록
    // 메뉴 등록
    public Menu addMenu(User loginUser, Long storeId, String name, BigDecimal price, String description) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("식당을 찾을 수 없습니다."));

        if (!store.getOwner().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 식당만 메뉴를 등록할 수 있습니다.");
        }

        Menu menu = Menu.builder()
                .store(store)
                .name(name)
                .price(price)
                .description(description)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        // 저장된 메뉴 확인
        System.out.println("Saved Menu: " + savedMenu);

        return savedMenu;
    }


    // 메뉴 수정
    public Menu updateMenu(User loginUser, Long menuId, String name, BigDecimal price, String description) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));

        if (!menu.getStore().getOwner().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 식당 메뉴만 수정 가능합니다.");
        }

        menu.setName(name);
        menu.setPrice(price);
        menu.setDescription(description);

        return menuRepository.save(menu);
    }

    // 메뉴 삭제
    public void deleteMenu(User loginUser, Long menuId) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));

        if (!menu.getStore().getOwner().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 식당 메뉴만 삭제 가능합니다.");
        }

        menuRepository.delete(menu);
    }

    // 식당 메뉴 조회
    @Transactional(readOnly = true)
    public List<Menu> listMenus(Long storeId) {
        return menuRepository.findByStoreId(storeId);
    }

    // ✅ 로그인한 주인이 등록한 식당 목록 조회
    @Transactional(readOnly = true)
    public List<Store> getMyStores(User loginUser) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");
        return storeRepository.findByOwnerId(loginUser.getId());
    }

    // 식당 삭제
    public void deleteStore(User loginUser, Long storeId) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("식당을 찾을 수 없습니다."));

        if (!store.getOwner().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 식당만 삭제할 수 있습니다.");
        }

        storeRepository.delete(store);  // ✅ cascade 때문에 메뉴도 같이 삭제됨
    }

    @Transactional(readOnly = true)
    public Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));
    }

    // 로그인한 사용자 기준 식당 조회
    @Transactional(readOnly = true)
    public List<Store> getStoresByRole(User loginUser) {
        if (loginUser == null) throw new RuntimeException("로그인이 필요합니다.");

        if ("OWNER".equals(loginUser.getRole().name())) {
            // 본인이 등록한 식당만 조회
            return storeRepository.findByOwnerId(loginUser.getId());
        } else {
            // CUSTOMER → 모든 식당 조회
            return storeRepository.findAll();
        }
    }



}
