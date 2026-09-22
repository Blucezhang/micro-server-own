package com.own.face.trade;

import java.util.UUID;

/** Header contract shared by servlet services and the WebFlux gateway. */
public final class TradeCorrelation {
    public static final String HEADER = "X-Correlation-Id";

    private TradeCorrelation() {
    }

    public static String resolve(String requested) {
        String value = requested == null ? "" : requested.trim();
        return value.matches("[A-Za-z0-9._:-]{1,100}") ? value : UUID.randomUUID().toString();
    }
}
