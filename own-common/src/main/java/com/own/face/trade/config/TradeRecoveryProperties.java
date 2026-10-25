package com.own.face.trade.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trade.recovery")
public record TradeRecoveryProperties(int maxAttempts, long retryDelayMillis, long scanDelayMillis) {
    private static final long MIN_DELAY_MILLIS = Duration.ofSeconds(1).toMillis();
    private static final long MAX_DELAY_MILLIS = Duration.ofDays(1).toMillis();

    public TradeRecoveryProperties {
        if (maxAttempts < 1) throw new IllegalArgumentException("trade.recovery.max-attempts must be positive");
        requireDelay(retryDelayMillis, "retry-delay-millis");
        requireDelay(scanDelayMillis, "scan-delay-millis");
    }

    private static void requireDelay(long delay, String name) {
        if (delay < MIN_DELAY_MILLIS || delay > MAX_DELAY_MILLIS) {
            throw new IllegalArgumentException("trade.recovery." + name + " must be 1 second..1 day");
        }
    }
}
