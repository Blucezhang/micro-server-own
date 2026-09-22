package com.own.product.review.moderation.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "prd_review_prohibited_term")
public class ReviewProhibitedTerm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "normalized_term", nullable = false, length = 100, unique = true) private String normalizedTerm;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "updated_by", nullable = false) private Long updatedBy;
    @Column(name = "updated_at", nullable = false) private Date updatedAt;

    protected ReviewProhibitedTerm() { }

    public ReviewProhibitedTerm(String normalizedTerm, Long systemUserId) {
        this.normalizedTerm = normalizedTerm;
        this.active = true;
        this.createdBy = systemUserId;
        this.updatedBy = systemUserId;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    public void update(String normalizedTerm, boolean active, Long systemUserId) {
        this.normalizedTerm = normalizedTerm;
        this.active = active;
        this.updatedBy = systemUserId;
        this.updatedAt = new Date();
    }

    public Long getId() { return id; }
    public String getNormalizedTerm() { return normalizedTerm; }
    public boolean isActive() { return active; }
    public Long getCreatedBy() { return createdBy; }
    public Date getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public Date getUpdatedAt() { return updatedAt; }
}
