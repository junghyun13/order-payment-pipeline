package com.example.orderpay.store.repository;

import com.example.orderpay.store.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStoreId(Long storeId);
    List<Menu> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name);

}