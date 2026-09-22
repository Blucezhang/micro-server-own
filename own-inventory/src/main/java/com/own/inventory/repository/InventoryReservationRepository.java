package com.own.inventory.repository;

import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.domain.ReservationStatus;
import java.util.Date;
import java.util.List;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    InventoryReservation findByOrderNoAndProductId(String orderNo, Long productId);
    List<InventoryReservation> findByOrderNoAndStatus(String orderNo, ReservationStatus status);
    List<InventoryReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Date expiresAt);

    @Query("select r.id from InventoryReservation r where r.status = ?1 and r.expiresAt < ?2")
    List<Long> findIdsByStatusAndExpiresAtBefore(ReservationStatus status, Date expiresAt);

    @Query("select r.id from InventoryReservation r where r.orderNo = ?1 and r.status = ?2")
    List<Long> findIdsByOrderNoAndStatus(String orderNo, ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from InventoryReservation r where r.id = ?1")
    InventoryReservation findByIdForUpdate(Long id);
}
