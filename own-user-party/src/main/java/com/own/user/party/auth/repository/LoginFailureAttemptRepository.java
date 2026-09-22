package com.own.user.party.auth.repository;

import com.own.user.party.auth.domain.LoginFailureAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

public interface LoginFailureAttemptRepository extends JpaRepository<LoginFailureAttempt, Long> {
    LoginFailureAttempt findByLoginName(String loginName);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from LoginFailureAttempt a where a.loginName = ?1")
    LoginFailureAttempt findByLoginNameForUpdate(String loginName);
    @Modifying
    @Query(value = "INSERT IGNORE INTO auth_login_failure (login_name, failure_count, updated_at) VALUES (?1, 0, NOW())", nativeQuery = true)
    int insertIfAbsent(String loginName);

    /** Retain recent records and never remove an account that is still locked. */
    @Modifying
    @Transactional
    @Query("delete from LoginFailureAttempt a where a.updatedAt < ?1 and (a.lockedUntil is null or a.lockedUntil < ?2)")
    int deleteInactiveBefore(Date retentionCutoff, Date now);
}
