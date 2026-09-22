package com.own.user.party.auth.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Server-side refresh-session ledger. Only the random JWT id, never the raw token, is stored. */
@Entity
@Table(name = "auth_refresh_session")
public class RefreshTokenSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "token_id", nullable = false, unique = true, length = 64) private String tokenId;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "actor_type", nullable = false, length = 16) private String actorType;
    @Column(name = "expires_at", nullable = false) private Date expiresAt;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "replaced_by", length = 64) private String replacedBy;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "revoked_at") private Date revokedAt;
    protected RefreshTokenSession() { }
    public RefreshTokenSession(String tokenId, Long userId, String actorType, Date expiresAt) {
        this.tokenId = tokenId; this.userId = userId; this.actorType = actorType; this.expiresAt = expiresAt;
        this.status = "ACTIVE"; this.createdAt = new Date();
    }
    public boolean isActiveAt(Date now) { return "ACTIVE".equals(status) && expiresAt.after(now); }
    public void replace(String nextTokenId) { this.status = "REPLACED"; this.replacedBy = nextTokenId; this.revokedAt = new Date(); }
    public void revoke() { if ("ACTIVE".equals(status)) { this.status = "REVOKED"; this.revokedAt = new Date(); } }
    public String getTokenId() { return tokenId; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getActorType() { return actorType; }
    public Date getExpiresAt() { return expiresAt; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
}
