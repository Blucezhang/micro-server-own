package com.own.user.party.auth.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persistent account-level throttle; it never stores submitted passwords or source IPs. */
@Entity
@Table(name = "auth_login_failure")
public class LoginFailureAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "login_name", nullable = false, unique = true, length = 128) private String loginName;
    @Column(name = "failure_count", nullable = false) private Integer failureCount;
    @Column(name = "locked_until") private Date lockedUntil;
    @Column(name = "last_failure_at") private Date lastFailureAt;
    @Column(name = "updated_at", nullable = false) private Date updatedAt;
    protected LoginFailureAttempt() { }
    public LoginFailureAttempt(String loginName) { this.loginName = loginName; this.failureCount = 0; this.updatedAt = new Date(); }
    public boolean lockedAt(Date now) { return lockedUntil != null && lockedUntil.after(now); }
    public void fail(int threshold, Date lockedUntil) { failureCount = failureCount + 1; lastFailureAt = new Date(); updatedAt = lastFailureAt; if (failureCount >= threshold) this.lockedUntil = lockedUntil; }
    public void reset() { failureCount = 0; lockedUntil = null; updatedAt = new Date(); }
    public String getLoginName() { return loginName; } public Integer getFailureCount() { return failureCount; } public Date getLockedUntil() { return lockedUntil; }
}
