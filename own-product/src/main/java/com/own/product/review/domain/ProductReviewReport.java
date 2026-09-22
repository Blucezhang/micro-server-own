package com.own.product.review.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "prd_product_review_report")
public class ProductReviewReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "review_id", nullable = false) private Long reviewId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "reporter_id", nullable = false) private Long reporterId;
    @Column(nullable = false, length = 200) private String reason;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "resolution_note", length = 500) private String resolutionNote;
    @Column(name = "resolved_by") private Long resolvedBy;
    @Column(name = "reported_at", nullable = false) private Date reportedAt;
    @Column(name = "resolved_at") private Date resolvedAt;

    protected ProductReviewReport() { }
    public ProductReviewReport(Long reviewId, Long productId, Long reporterId, String reason) {
        this.reviewId = reviewId; this.productId = productId; this.reporterId = reporterId; this.reason = reason;
        this.status = "PENDING"; this.reportedAt = new Date();
    }
    public void resolve(Long systemUserId, String status, String note) {
        this.status = status; this.resolvedBy = systemUserId; this.resolutionNote = note; this.resolvedAt = new Date();
    }
    public Long getId() { return id; } public Long getReviewId() { return reviewId; } public Long getProductId() { return productId; }
    public Long getReporterId() { return reporterId; } public String getReason() { return reason; } public String getStatus() { return status; }
    public String getResolutionNote() { return resolutionNote; } public Long getResolvedBy() { return resolvedBy; }
    public Date getReportedAt() { return reportedAt; } public Date getResolvedAt() { return resolvedAt; }
}
