package com.own.face.security;

import com.own.face.trade.ActorType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable identity extracted from a verified marketplace access token. */
public final class JwtPrincipal {
    private final Long userId;
    private final Long actorId;
    private final ActorType actorType;
    private final List<String> roles;
    private final List<String> permissions;

    public JwtPrincipal(Long userId, Long actorId, ActorType actorType,
                        List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.actorId = actorId;
        this.actorType = actorType;
        this.roles = immutableCopy(roles);
        this.permissions = immutableCopy(permissions);
    }

    public Long getUserId() { return userId; }
    public Long getActorId() { return actorId; }
    public ActorType getActorType() { return actorType; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }

    private static List<String> immutableCopy(List<String> source) {
        return Collections.unmodifiableList(source == null ? new ArrayList<String>() : new ArrayList<String>(source));
    }
}
