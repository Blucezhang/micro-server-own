package com.own.promotion.coupon.repository;

import com.own.promotion.coupon.domain.CouponStock;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CouponStockRepository extends JpaRepository<CouponStock, Long> {
    CouponStock findByScopeKey(String scopeKey);

    /** MySQL INSERT IGNORE makes first-claim initialization safe under concurrent requests. */
    @Modifying
    @Query(value = "INSERT IGNORE INTO mkt_coupon_stock (scope_key, total_quantity, available_quantity, version) VALUES (?1, ?2, ?2, 0)", nativeQuery = true)
    int insertIfAbsent(String scopeKey, Integer totalQuantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CouponStock s where s.scopeKey = ?1")
    CouponStock findByScopeKeyForUpdate(String scopeKey);
}
