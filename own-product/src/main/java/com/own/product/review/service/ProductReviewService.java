package com.own.product.review.service;

import com.own.face.trade.TradeException;
import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.review.domain.ProductReview;
import com.own.product.review.domain.ProductReviewReport;
import com.own.product.review.dto.CreateProductReviewCommand;
import com.own.product.review.dto.CreateProductReviewReportCommand;
import com.own.product.review.dto.ReviewReplyCommand;
import com.own.product.review.dto.ModerateReviewCommand;
import com.own.product.review.dto.ResolveProductReviewReportCommand;
import com.own.product.review.dto.PublicProductReviewView;
import com.own.product.review.repository.ProductReviewRepository;
import com.own.product.review.repository.ProductReviewReportRepository;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductReviewService {
    private final ProductReviewRepository reviews; private final ProductReviewReportRepository reports; private final ProductDao products; private final OrderReviewEligibilityClient eligibility; private final ReviewContentPolicy contentPolicy;
    @org.springframework.beans.factory.annotation.Autowired public ProductReviewService(ProductReviewRepository reviews, ProductReviewReportRepository reports, ProductDao products, OrderReviewEligibilityClient eligibility, ReviewContentPolicy contentPolicy) { this.reviews = reviews; this.reports = reports; this.products = products; this.eligibility = eligibility; this.contentPolicy = contentPolicy; }
    public ProductReviewService(ProductReviewRepository reviews, ProductReviewReportRepository reports, ProductDao products, OrderReviewEligibilityClient eligibility) { this(reviews, reports, products, eligibility, new ReviewContentPolicy("")); }
    @Transactional public ProductReview create(Long buyerId, Long productId, CreateProductReviewCommand command) {
        if (buyerId == null || buyerId.longValue() <= 0 || productId == null || productId.longValue() <= 0) throw TradeException.unprocessable("buyerId and productId are required");
        if (command == null || command.getRating() == null || command.getRating() < 1 || command.getRating() > 5) throw TradeException.unprocessable("rating must be between 1 and 5");
        String content = command.getContent() == null ? "" : command.getContent().trim(); if (content.length() > 500) throw TradeException.unprocessable("review content must not exceed 500 characters");
        contentPolicy.requireAllowed(content); if (products.queryProductById(productId) == null) throw TradeException.notFound("product was not found");
        if (!eligibility.hasReceivedProduct(buyerId, productId)) throw TradeException.forbidden("only buyers with a received product can review it");
        if (reviews.findByBuyerIdAndProductId(buyerId, productId) != null) throw TradeException.conflict("product has already been reviewed by this buyer");
        return reviews.save(new ProductReview(buyerId, productId, command.getRating(), content));
    }
    public List<ProductReview> list(Long productId) { return reviews.findByProductIdAndStatusOrderByIdDesc(productId, "PUBLISHED"); }
    public Map<String, Object> page(Long productId, int page, int size) {
        if (productId == null || productId.longValue() <= 0) throw TradeException.unprocessable("productId is required");
        if (page < 0 || size < 1 || size > 100) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100");
        Page<ProductReview> result = reviews.findByProductIdAndStatusOrderByIdDesc(productId, "PUBLISHED", PageRequest.of(page, size));
        List<PublicProductReviewView> items = new ArrayList<PublicProductReviewView>(); for (ProductReview review : result.getContent()) items.add(publicView(review));
        Map<String, Object> response = new HashMap<String, Object>(); response.put("total", result.getTotalElements()); response.put("page", page); response.put("size", size); response.put("items", items); return response;
    }
    @Transactional public ProductReview reply(Long merchantId, Long reviewId, ReviewReplyCommand command) {
        ProductReview review = require(reviewId); Product product = products.queryProductById(review.getProductId()); if (product == null || !merchantId.equals(product.getPartyId())) throw TradeException.forbidden("merchant does not own review product");
        String content = command == null || command.getContent() == null ? "" : command.getContent().trim(); if (content.isEmpty() || content.length() > 500) throw TradeException.unprocessable("reply content must be 1..500 characters"); contentPolicy.requireAllowed(content);
        review.reply(content); return reviews.save(review);
    }
    public PublicProductReviewView publicView(ProductReview review) { return new PublicProductReviewView(review); }
    @Transactional public ProductReview moderate(Long productId, Long reviewId, ModerateReviewCommand command) {
        if (command == null || command.getPublished() == null) throw TradeException.unprocessable("published is required"); ProductReview review = require(reviewId); if (!productId.equals(review.getProductId())) throw TradeException.notFound("product review was not found"); review.setPublished(command.getPublished()); return reviews.save(review);
    }
    @Transactional public ProductReviewReport report(Long buyerId, Long productId, Long reviewId, CreateProductReviewReportCommand command) {
        if (buyerId == null || buyerId.longValue() <= 0) throw TradeException.unprocessable("buyerId is required");
        ProductReview review = require(reviewId); if (!productId.equals(review.getProductId()) || !"PUBLISHED".equals(review.getStatus())) throw TradeException.notFound("published product review was not found");
        String reason = command == null || command.getReason() == null ? "" : command.getReason().trim();
        if (reason.isEmpty() || reason.length() > 200) throw TradeException.unprocessable("report reason must be 1..200 characters");
        if (reports.findByReviewIdAndReporterId(reviewId, buyerId) != null) throw TradeException.conflict("review has already been reported by this buyer");
        return reports.save(new ProductReviewReport(reviewId, productId, buyerId, reason));
    }
    public Map<String, Object> reportPage(String status, int page, int size) {
        if (!"PENDING".equals(status) && !"RESOLVED".equals(status) && !"DISMISSED".equals(status)) throw TradeException.unprocessable("status must be PENDING, RESOLVED or DISMISSED");
        if (page < 0 || size < 1 || size > 100) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100");
        Page<ProductReviewReport> result = reports.findByStatusOrderByIdDesc(status, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<String, Object>(); response.put("total", result.getTotalElements()); response.put("page", page); response.put("size", size); response.put("items", result.getContent()); return response;
    }
    @Transactional public ProductReviewReport resolveReport(Long systemUserId, Long reportId, ResolveProductReviewReportCommand command) {
        if (systemUserId == null || systemUserId.longValue() <= 0) throw TradeException.unprocessable("system user is required");
        ProductReviewReport report = reportId == null ? null : reports.findById(reportId).orElse(null); if (report == null) throw TradeException.notFound("product review report was not found");
        String status = command == null || command.getStatus() == null ? "" : command.getStatus().trim(); if (!"RESOLVED".equals(status) && !"DISMISSED".equals(status)) throw TradeException.unprocessable("status must be RESOLVED or DISMISSED");
        String note = command.getNote() == null ? "" : command.getNote().trim(); if (note.length() > 500) throw TradeException.unprocessable("resolution note must not exceed 500 characters");
        report.resolve(systemUserId, status, note); return reports.save(report);
    }
    private ProductReview require(Long id) { ProductReview review = id == null ? null : reviews.findById(id).orElse(null); if (review == null) throw TradeException.notFound("product review was not found"); return review; }
}
