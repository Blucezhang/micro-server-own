package com.own.product.review.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

class ReviewRouteContractTest {
    @Test
    void reviewRoutesRetainTheProductPrefixUsedByTheGateway() {
        assertRoute(ProductReviewController.class, "/product/api/v1/products/{productId}/reviews");
        assertRoute(MerchantReviewController.class, "/product/api/v1/merchant/reviews");
        assertRoute(SystemProductReviewReportController.class, "/product/api/v1/system/review-reports");
        assertRoute(SystemReviewProhibitedTermController.class, "/product/api/v1/system/review-prohibited-terms");
    }

    private void assertRoute(Class<?> controller, String path) {
        assertArrayEquals(new String[] { path }, controller.getAnnotation(RequestMapping.class).value());
    }
}
