package com.own.product.review.repository;

import com.own.product.review.domain.ProductReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewReportRepository extends JpaRepository<ProductReviewReport, Long> {
    ProductReviewReport findById(Long id);
    ProductReviewReport findByReviewIdAndReporterId(Long reviewId, Long reporterId);
    Page<ProductReviewReport> findByStatusOrderByIdDesc(String status, Pageable pageable);
}
