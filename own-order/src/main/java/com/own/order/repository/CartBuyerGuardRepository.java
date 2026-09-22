package com.own.order.repository;

import com.own.order.domain.CartBuyerGuard;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartBuyerGuardRepository extends JpaRepository<CartBuyerGuard, Long> {
    @Modifying
    @Query(value = "INSERT IGNORE INTO ord_cart_buyer_guard (buyer_id) VALUES (?1)", nativeQuery = true)
    void ensure(Long buyerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from CartBuyerGuard g where g.buyerId = ?1")
    CartBuyerGuard findByBuyerIdForUpdate(Long buyerId);
}
