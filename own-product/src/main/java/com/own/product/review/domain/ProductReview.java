package com.own.product.review.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prd_product_review")
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private Integer rating;
    @Column(nullable = false, length = 500) private String content;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "merchant_reply", length = 500) private String merchantReply;
    @Column(name = "replied_at") private Date repliedAt;
    @Column(name = "moderated_at") private Date moderatedAt;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected ProductReview() { }
    public ProductReview(Long buyerId, Long productId, Integer rating, String content) {
        this.buyerId = buyerId; this.productId = productId; this.rating = rating; this.content = content; this.status = "PUBLISHED"; this.createdAt = new Date();
    }
    public void reply(String reply) { this.merchantReply = reply; this.repliedAt = new Date(); }
    public void setPublished(boolean published) { this.status = published ? "PUBLISHED" : "HIDDEN"; this.moderatedAt = new Date(); }
    public Long getId() { return id; } public Long getBuyerId() { return buyerId; } public Long getProductId() { return productId; }
    public Integer getRating() { return rating; } public String getContent() { return content; } public String getStatus() { return status; } public String getMerchantReply() { return merchantReply; } public Date getRepliedAt() { return repliedAt; } public Date getModeratedAt() { return moderatedAt; } public Date getCreatedAt() { return createdAt; }
}
