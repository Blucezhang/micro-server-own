package com.own.product.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.review.domain.ProductReview;
import com.own.product.review.dto.CreateProductReviewCommand;
import com.own.product.review.dto.CreateProductReviewReportCommand;
import com.own.product.review.domain.ProductReviewReport;
import com.own.product.review.dto.PublicProductReviewView;
import com.own.product.review.dto.ReviewReplyCommand;
import com.own.product.review.repository.ProductReviewRepository;
import com.own.product.review.repository.ProductReviewReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ProductReviewServiceTest {
    @Test
    public void receivedBuyerCanPublishOneReview() {
        ProductReviewRepository reviews = mock(ProductReviewRepository.class);
        ProductDao products = mock(ProductDao.class);
        OrderReviewEligibilityClient eligibility = mock(OrderReviewEligibilityClient.class);
        when(products.queryProductById(3L)).thenReturn(new Product());
        when(eligibility.hasReceivedProduct(1L, 3L)).thenReturn(true);
        when(reviews.save(any(ProductReview.class))).thenAnswer(i -> i.getArguments()[0]);
        CreateProductReviewCommand command = new CreateProductReviewCommand(); command.setRating(5); command.setContent("good");
        ProductReview created = new ProductReviewService(reviews, mock(ProductReviewReportRepository.class), products, eligibility).create(1L, 3L, command);
        assertSame(Integer.valueOf(5), created.getRating());
    }

    @Test
    public void merchantCanReplyOnlyToOwnProductReview() {
        ProductReviewRepository reviews = mock(ProductReviewRepository.class);
        ProductDao products = mock(ProductDao.class);
        ProductReview review = new ProductReview(1L, 3L, 5, "good"); ReflectionTestUtils.setField(review, "id", 9L);
        Product product = new Product(); product.setPartyId(8L);
        when(reviews.findById(9L)).thenReturn(Optional.of(review)); when(products.queryProductById(3L)).thenReturn(product); when(reviews.save(review)).thenReturn(review);
        ReviewReplyCommand command = new ReviewReplyCommand(); command.setContent("thanks");
        ProductReview result = new ProductReviewService(reviews, mock(ProductReviewReportRepository.class), products, mock(OrderReviewEligibilityClient.class)).reply(8L, 9L, command);
        assertSame("thanks", result.getMerchantReply());
    }

    @Test
    public void publicReviewPageExposesOnlyRepositoryPageAndMetadata() {
        ProductReviewRepository reviews = mock(ProductReviewRepository.class);
        ProductReview first = new ProductReview(1L, 3L, 5, "good");
        when(reviews.findByProductIdAndStatusOrderByIdDesc(org.mockito.ArgumentMatchers.eq(3L), org.mockito.ArgumentMatchers.eq("PUBLISHED"), any(Pageable.class)))
                .thenReturn(new PageImpl<ProductReview>(Arrays.asList(first)));
        Map<String, Object> result = new ProductReviewService(reviews, mock(ProductReviewReportRepository.class), mock(ProductDao.class), mock(OrderReviewEligibilityClient.class)).page(3L, 0, 20);
        assertEquals(1L, result.get("total"));
        assertEquals(1, ((java.util.List) result.get("items")).size());
        assertEquals(PublicProductReviewView.class, ((java.util.List) result.get("items")).get(0).getClass());
    }

    @Test
    public void buyerCanReportPublishedReviewOnceWithReason() {
        ProductReviewRepository reviews = mock(ProductReviewRepository.class);
        ProductReviewReportRepository reports = mock(ProductReviewReportRepository.class);
        ProductReview review = new ProductReview(2L, 3L, 1, "bad content"); ReflectionTestUtils.setField(review, "id", 9L);
        when(reviews.findById(9L)).thenReturn(Optional.of(review));
        when(reports.save(any(ProductReviewReport.class))).thenAnswer(i -> i.getArguments()[0]);
        CreateProductReviewReportCommand command = new CreateProductReviewReportCommand(); command.setReason("abusive language");
        ProductReviewReport created = new ProductReviewService(reviews, reports, mock(ProductDao.class), mock(OrderReviewEligibilityClient.class)).report(1L, 3L, 9L, command);
        assertEquals("PENDING", created.getStatus());
        assertEquals("abusive language", created.getReason());
    }

    @Test
    public void prohibitedTermsRejectReviewBeforeItIsSaved() {
        ProductReviewRepository reviews = mock(ProductReviewRepository.class); ProductDao products = mock(ProductDao.class);
        when(products.queryProductById(3L)).thenReturn(new Product());
        when(mock(OrderReviewEligibilityClient.class).hasReceivedProduct(1L, 3L)).thenReturn(true);
        CreateProductReviewCommand command = new CreateProductReviewCommand(); command.setRating(5); command.setContent("contains blocked phrase");
        assertThrows(com.own.face.trade.TradeException.class,
                () -> new ProductReviewService(reviews, mock(ProductReviewReportRepository.class), products,
                        mock(OrderReviewEligibilityClient.class), new ReviewContentPolicy("blocked phrase"))
                        .create(1L, 3L, command));
    }
}
