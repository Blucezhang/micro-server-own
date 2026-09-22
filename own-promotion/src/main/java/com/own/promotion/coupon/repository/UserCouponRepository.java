package com.own.promotion.coupon.repository;

import com.own.promotion.coupon.domain.CouponStatus;
import com.own.promotion.coupon.domain.UserCoupon;
import java.util.List;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import javax.persistence.LockModeType;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    UserCoupon findByCouponNo(String couponNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from UserCoupon c where c.couponNo = ?1")
    UserCoupon findByCouponNoForUpdate(String couponNo);
    List<UserCoupon> findByBuyerIdOrderByIdDesc(Long buyerId);
    UserCoupon findByBuyerIdAndSourceKey(Long buyerId, String sourceKey);
    List<UserCoupon> findByReservedOrderNoAndStatus(String orderNo, CouponStatus status);
    List<UserCoupon> findByStatusAndReservationExpiresAtBefore(CouponStatus status, Date reservationExpiresAt);
    long countByMerchantId(Long merchantId);
    long countByMerchantIdAndStatus(Long merchantId, CouponStatus status);
}
