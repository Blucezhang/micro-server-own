package com.own.face.security;

import com.own.face.trade.ActorType;
import java.util.Date;

/** Verified, single-purpose refresh-token claims. It is never accepted at the API gateway. */
public final class JwtRefreshPrincipal {
    private final Long userId;
    private final ActorType actorType;
    private final String tokenId;
    private final Date expiresAt;

    public JwtRefreshPrincipal(Long userId, ActorType actorType, String tokenId, Date expiresAt) {
        this.userId = userId; this.actorType = actorType; this.tokenId = tokenId; this.expiresAt = expiresAt;
    }
    public Long getUserId() { return userId; }
    public ActorType getActorType() { return actorType; }
    public String getTokenId() { return tokenId; }
    public Date getExpiresAt() { return expiresAt; }
}
