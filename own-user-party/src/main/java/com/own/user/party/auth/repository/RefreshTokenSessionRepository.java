package com.own.user.party.auth.repository;

import com.own.user.party.auth.domain.RefreshTokenSession;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RefreshTokenSession s where s.tokenId = ?1")
    RefreshTokenSession findByTokenIdForUpdate(String tokenId);

    @Modifying
    @Transactional
    @Query("delete from RefreshTokenSession s where s.expiresAt < ?1")
    int deleteExpiredBefore(Date cutoff);

    List<RefreshTokenSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RefreshTokenSession s where s.id = ?1")
    RefreshTokenSession findByIdForUpdate(Long id);

    @Modifying
    @Transactional
    @Query("update RefreshTokenSession s set s.status = 'REVOKED', s.revokedAt = ?2 where s.userId = ?1 and s.status = 'ACTIVE'")
    int revokeActiveByUserId(Long userId, Date revokedAt);
}
