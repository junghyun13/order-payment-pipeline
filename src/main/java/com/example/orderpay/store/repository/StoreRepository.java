package com.example.orderpay.store.repository;

import com.example.orderpay.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOwnerId(Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
