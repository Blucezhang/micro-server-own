package com.own.face.trade.config;

/** Shared reservation API safety boundary, not the deployment's default TTL. */
public final class ReservationLimits {
    public static final int MAX_TTL_MINUTES = 60;

    private ReservationLimits() { }
}
