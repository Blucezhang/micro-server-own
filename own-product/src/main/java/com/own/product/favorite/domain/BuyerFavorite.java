package com.own.product.favorite.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "prd_buyer_favorite")
public class BuyerFavorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected BuyerFavorite() { }
    public BuyerFavorite(Long buyerId, Long productId) { this.buyerId = buyerId; this.productId = productId; this.createdAt = new Date(); }
    public Long getId() { return id; } public Long getBuyerId() { return buyerId; } public Long getProductId() { return productId; } public Date getCreatedAt() { return createdAt; }
}
