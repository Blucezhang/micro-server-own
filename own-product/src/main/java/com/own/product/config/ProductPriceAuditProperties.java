package com.own.product.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("own.product.price-audit")
public record ProductPriceAuditProperties(int batchSize, long recoveryDelayMs) {
    /** Hard safety cap; changing deployment configuration must not create an unbounded scan. */
    public static final int MAX_BATCH_SIZE = 50;

    public ProductPriceAuditProperties {
        if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("price-audit.batch-size must be 1.." + MAX_BATCH_SIZE);
        }
        if (recoveryDelayMs < Duration.ofSeconds(1).toMillis() || recoveryDelayMs > Duration.ofDays(1).toMillis()) {
            throw new IllegalArgumentException("price-audit.recovery-delay-ms must be 1 second..1 day");
        }
    }
}
