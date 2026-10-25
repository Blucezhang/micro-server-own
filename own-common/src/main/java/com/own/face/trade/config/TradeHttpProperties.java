package com.own.face.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Runtime defaults belong to each service's runtime YAML, not to Java callers. */
@ConfigurationProperties("trade.http")
public record TradeHttpProperties(int connectTimeoutMillis, int readTimeoutMillis) {
    private static final int MAX_TIMEOUT_MILLIS = 60_000;

    public TradeHttpProperties {
        requireBounded(connectTimeoutMillis);
        requireBounded(readTimeoutMillis);
    }

    private static void requireBounded(int timeout) {
        if (timeout < 1 || timeout > MAX_TIMEOUT_MILLIS) {
            throw new IllegalArgumentException("trade.http timeouts must be 1.." + MAX_TIMEOUT_MILLIS + " ms");
        }
    }
}
