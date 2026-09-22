package com.own.user.party.service;

import com.own.face.trade.ActorType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Authorization result assembled from the legacy role/function graph. */
public final class LoginAuthorization {
    private final Long actorId;
    private final ActorType actorType;
    private final List<String> roles;
    private final List<String> permissions;

    public LoginAuthorization(Long actorId, ActorType actorType, List<String> roles, List<String> permissions) {
        this.actorId = actorId;
        this.actorType = actorType;
        this.roles = Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
    }
    public Long getActorId() { return actorId; }
    public ActorType getActorType() { return actorType; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }
}
