package com.own.product.review.dto;

import com.own.product.review.domain.ProductReview;
import java.util.Date;

/** Safe projection for public and merchant review responses; it deliberately omits buyer identity and moderation state. */
public class PublicProductReviewView {
    private final Long id;
    private final Long productId;
    private final Integer rating;
    private final String content;
    private final String merchantReply;
    private final Date repliedAt;
    private final Date createdAt;

    public PublicProductReviewView(ProductReview review) {
        this.id = review.getId(); this.productId = review.getProductId(); this.rating = review.getRating(); this.content = review.getContent();
        this.merchantReply = review.getMerchantReply(); this.repliedAt = review.getRepliedAt(); this.createdAt = review.getCreatedAt();
    }
    public Long getId() { return id; } public Long getProductId() { return productId; } public Integer getRating() { return rating; }
    public String getContent() { return content; } public String getMerchantReply() { return merchantReply; }
    public Date getRepliedAt() { return repliedAt; } public Date getCreatedAt() { return createdAt; }
}
