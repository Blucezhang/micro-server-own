package com.own.inventory.repository;

import com.own.inventory.domain.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import javax.persistence.LockModeType;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    InventoryStock findByProductIdAndMerchantId(Long productId, Long merchantId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    InventoryStock findLockedByProductIdAndMerchantId(Long productId, Long merchantId);

    /** Atomic MySQL initialization: a repeated catalogue import must never reset live balances. */
    @Modifying
    @Query(value = "INSERT IGNORE INTO inv_stock (product_id, merchant_id, available_quantity, reserved_quantity, sold_quantity, version) VALUES (?1, ?2, ?3, 0, 0, 0)", nativeQuery = true)
    int insertIfAbsent(Long productId, Long merchantId, Integer availableQuantity);
}
