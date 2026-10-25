package com.own.product.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@NoArgsConstructor
@Node("ProductPriceChangeEvent")
public class ProductPriceChangeEvent {

    @Id
    @GeneratedValue
    private Long id;

    @Property(name = "eventKey")
    private String eventKey;

    @Property(name = "requestFingerprint")
    private String requestFingerprint;

    @Property(name = "productId")
    private Long productId;

    @Property(name = "merchantId")
    private Long merchantId;

    @Property(name = "originalPriceBefore")
    private String originalPriceBefore;

    @Property(name = "originalPriceAfter")
    private String originalPriceAfter;

    @Property(name = "promotionPriceBefore")
    private String promotionPriceBefore;

    @Property(name = "promotionPriceAfter")
    private String promotionPriceAfter;

    @Property(name = "reason")
    private String reason;

    @Property(name = "changedAtEpochSecond")
    private Long changedAtEpochSecond;

    @Property(name = "productSnapshot")
    private String productSnapshot;

    @Property(name = "priceChanged")
    private Boolean priceChanged;

    @Property(name = "projected")
    private Boolean projected;

    @Property(name = "projectedAtEpochSecond")
    private Long projectedAtEpochSecond;

    public ProductPriceChangeEvent(
            String eventKey,
            String requestFingerprint,
            Long productId,
            Long merchantId,
            String originalPriceBefore,
            String originalPriceAfter,
            String promotionPriceBefore,
            String promotionPriceAfter,
            String reason,
            Long changedAtEpochSecond,
            String productSnapshot,
            Boolean priceChanged,
            Boolean projected) {
        this.eventKey = eventKey;
        this.requestFingerprint = requestFingerprint;
        this.productId = productId;
        this.merchantId = merchantId;
        this.originalPriceBefore = originalPriceBefore;
        this.originalPriceAfter = originalPriceAfter;
        this.promotionPriceBefore = promotionPriceBefore;
        this.promotionPriceAfter = promotionPriceAfter;
        this.reason = reason;
        this.changedAtEpochSecond = changedAtEpochSecond;
        this.productSnapshot = productSnapshot;
        this.priceChanged = priceChanged;
        this.projected = projected;
    }
}
