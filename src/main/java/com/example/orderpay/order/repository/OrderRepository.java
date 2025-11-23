package com.example.orderpay.order.repository;

import com.example.orderpay.order.domain.Order;
import com.example.orderpay.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatus(Long storeId, OrderStatus status, Pageable pageable);

    long countByStoreIdAndStatus(Long storeId, OrderStatus status);
    long countByStoreId(Long storeId);

    List<Order> findTop10ByStoreIdOrderByCreatedAtDesc(Long storeId);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.storeId = :storeId")

    BigDecimal sumTotalAmountByStoreId(@Param("storeId") Long storeId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.userId = :userId AND o.status = :status")
    List<Order> findByStoreIdAndUserIdAndStatus(
            @Param("storeId") Long storeId,
            @Param("userId") Long userId,
            @Param("status") OrderStatus status
    );


    // 이미 존재:
    List<Order> findByStoreIdAndStatusAndCreatedAtBetween(
            Long storeId, OrderStatus status, OffsetDateTime start, OffsetDateTime end);


    // OWNER 단건 합계 조회용
    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.storeId = :storeId and o.status = :status and o.createdAt between :start and :end")
    BigDecimal sumTotalAmountByStoreIdAndStatusAndCreatedAtBetween(
            @Param("storeId") Long storeId,
            @Param("status") OrderStatus status,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    Page<Order> findByStoreIdAndUserId(Long storeId, Long userId, Pageable pageable);




    long countByStoreIdAndStatusAndCreatedAtAfter(Long storeId, OrderStatus status, OffsetDateTime since);

    long countByStoreIdAndCreatedAtAfter(Long storeId, OffsetDateTime since);

    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.storeId = :storeId AND o.createdAt >= :since")
    BigDecimal sumTotalAmountByStoreIdSince(@Param("storeId") Long storeId,
                                            @Param("since") OffsetDateTime since);

    List<Order> findTop10ByStoreIdAndCreatedAtAfterOrderByCreatedAtDesc(Long storeId, OffsetDateTime since);


}