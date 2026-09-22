package com.own.product.review.service;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Calls the order service rather than trusting a client-side purchase claim. */
@Component
public class OrderReviewEligibilityClient {
    private final RestTemplate restTemplate; private final String internalToken;
    public OrderReviewEligibilityClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }
    public boolean hasReceivedProduct(Long buyerId, Long productId) {
        if (internalToken == null || internalToken.trim().isEmpty()) throw new TradeException(503, "internal service token is not configured");
        HttpHeaders headers = new HttpHeaders(); headers.set(InternalServiceGuard.HEADER, internalToken); headers.set("X-Actor-Id", "1"); headers.set("X-Actor-Type", "SYSTEM");
        try {
            ResponseEntity<Resp> response = restTemplate.exchange("http://own-order/api/v1/internal/buyers/{buyerId}/products/{productId}/reviewable", HttpMethod.GET, new HttpEntity<Object>(headers), Resp.class, buyerId, productId);
            return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getData());
        } catch (RuntimeException exception) { throw new TradeException(503, "order review eligibility is unavailable"); }
    }
}
