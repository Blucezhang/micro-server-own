package com.own.user.party.address.repository;

import com.own.user.party.address.domain.BuyerAddressGuard;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BuyerAddressGuardRepository extends JpaRepository<BuyerAddressGuard, Long> {
    @Modifying
    @Query(value = "INSERT IGNORE INTO buyer_address_guard (buyer_id) VALUES (?1)", nativeQuery = true)
    void ensure(Long buyerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from BuyerAddressGuard g where g.buyerId = ?1")
    BuyerAddressGuard findByBuyerIdForUpdate(Long buyerId);
}
