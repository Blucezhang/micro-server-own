package com.own.product.review.repository;

import com.own.product.review.domain.ProductReview;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    ProductReview findByBuyerIdAndProductId(Long buyerId, Long productId);
    ProductReview findById(Long id);
    List<ProductReview> findByProductIdAndStatusOrderByIdDesc(Long productId, String status);
    Page<ProductReview> findByProductIdAndStatusOrderByIdDesc(Long productId, String status, Pageable pageable);
}
