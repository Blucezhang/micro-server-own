package com.own.face.trade;

public final class TradeActor {

    private final Long id;
    private final ActorType type;

    public TradeActor(Long id, ActorType type) {
        this.id = id;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public ActorType getType() {
        return type;
    }

    public void require(ActorType expected) {
        if (type != expected) {
            throw TradeException.forbidden("actor type " + expected + " is required");
        }
    }
}
