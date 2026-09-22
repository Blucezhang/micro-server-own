package com.own.product.review.moderation.repository;

import com.own.product.review.moderation.domain.ReviewProhibitedTerm;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewProhibitedTermRepository extends JpaRepository<ReviewProhibitedTerm, Long> {
    ReviewProhibitedTerm findById(Long id);
    ReviewProhibitedTerm findByNormalizedTerm(String normalizedTerm);
    List<ReviewProhibitedTerm> findByActiveTrueOrderByIdAsc();
    Page<ReviewProhibitedTerm> findAllByOrderByIdDesc(Pageable pageable);
}
