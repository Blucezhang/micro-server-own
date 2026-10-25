package com.own.order.service;

import com.own.face.trade.config.ReservationLimits;
import com.own.face.trade.config.ServiceEndpoint;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeResponse;
import com.own.face.security.InternalServiceGuard;
import com.own.order.domain.OrderItem;
import com.own.order.domain.AfterSaleItem;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalToken;
    private final int reservationTtlMinutes;

    public InventoryClient(RestTemplate restTemplate,
            @Value("${trade.internal.service-token:}") String internalToken,
            @Value("${trade.services.inventory-base-url}") String baseUrl,
            @Value("${trade.saga.reservation-ttl-minutes}") int reservationTtlMinutes) {
        this.restTemplate = restTemplate;
        this.baseUrl = ServiceEndpoint.requireRoot(baseUrl);
        this.internalToken = internalToken;
        if (reservationTtlMinutes < 1 || reservationTtlMinutes > ReservationLimits.MAX_TTL_MINUTES) {
            throw new IllegalArgumentException("reservation TTL must be 1.." + ReservationLimits.MAX_TTL_MINUTES + " minutes");
        }
        this.reservationTtlMinutes = reservationTtlMinutes;
    }

    public void reserve(String orderNo, OrderItem item) {
        reserve(orderNo, item.getProductId(), item.getMerchantId(), item.getQuantity());
    }

    /** One reservation exists for one order/SKU; callers aggregate duplicate cart rows first. */
    public void reserve(String orderNo, Long productId, Long merchantId, Integer quantity) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("orderNo", orderNo);
        body.put("productId", productId);
        body.put("merchantId", merchantId);
        body.put("quantity", quantity);
        body.put("ttlMinutes", reservationTtlMinutes);
        post(baseUrl + "/api/v1/internal/reservations", body,
                "order-inventory-reserve-" + orderNo + "-" + productId + "-" + merchantId);
    }

    public void commit(String orderNo, List<OrderItem> items) {
        post(baseUrl + "/api/v1/internal/orders/" + orderNo + "/inventory/commit",
                orderItemBody(items), "order-inventory-commit-" + orderNo);
    }
    public void reconcileCommit(String orderNo, List<OrderItem> items) {
        if (!confirmedEffect(orderNo, items, "COMMITTED")) commit(orderNo, items);
    }
    public void release(String orderNo) { post(baseUrl + "/api/v1/internal/orders/" + orderNo + "/inventory/release", null, "order-inventory-release-" + orderNo); }
    public void reconcileRelease(String orderNo, List<OrderItem> items) {
        if (!confirmedEffect(orderNo, items, "RELEASED")) release(orderNo);
    }
    public void refund(String orderNo, List<OrderItem> items) {
        if (confirmedEffect(orderNo, items, "REFUNDED")) return;
        post(baseUrl + "/api/v1/internal/orders/" + orderNo + "/inventory/refund",
                orderItemBody(items), "order-inventory-refund-" + orderNo);
    }

    private boolean confirmedEffect(String orderNo, List<OrderItem> items, String target) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/orders/" + orderNo + "/inventory/state",
                    HttpMethod.GET, new HttpEntity<Void>(internalHeaders("inventory-state-" + orderNo)), Map.class);
            Object data = TradeResponse.requireSuccess(response, "inventory recovery state").get("data");
            List<Map<String, Object>> expected = orderItemBody(items);
            if (!(data instanceof List<?> rows) || rows.size() != expected.size() || rows.isEmpty()) {
                throw TradeException.conflict("inventory recovery state does not match order items");
            }
            java.util.Set<Long> seen = new java.util.HashSet<>();
            boolean confirmed = true;
            for (Object raw : rows) {
                if (!(raw instanceof Map<?, ?> row) || !orderNo.equals(row.get("orderNo"))) {
                    throw TradeException.conflict("invalid inventory recovery row");
                }
                Long productId = Long.valueOf(String.valueOf(row.get("productId")));
                Map<String, Object> line = expected.stream().filter(item -> productId.equals(item.get("productId"))).findFirst().orElse(null);
                if (line == null || !seen.add(productId)
                        || !String.valueOf(line.get("merchantId")).equals(String.valueOf(row.get("merchantId")))
                        || !String.valueOf(line.get("quantity")).equals(String.valueOf(row.get("quantity")))) {
                    throw TradeException.conflict("inventory recovery row does not match order item");
                }
                String source = "REFUNDED".equals(target) ? "COMMITTED" : "RESERVED";
                boolean done = target.equals(row.get("status"))
                        || "RELEASED".equals(target) && "EXPIRED".equals(row.get("status"));
                if (!done && !source.equals(row.get("status"))) {
                    throw TradeException.conflict("inventory recovery requires reconciliation");
                }
                confirmed &= done;
            }
            return confirmed;
        } catch (TradeException exception) { throw exception;
        } catch (Exception exception) {
            TradeException failure = TradeException.conflict("inventory recovery state is unavailable");
            if (exception instanceof org.springframework.web.client.ResourceAccessException) failure.initCause(exception);
            throw failure;
        }
    }

    private List<Map<String, Object>> orderItemBody(List<OrderItem> items) {
        Map<Long, Map<String, Object>> byProduct = new LinkedHashMap<Long, Map<String, Object>>();
        for (OrderItem item : items) {
            Map<String, Object> line = byProduct.get(item.getProductId());
            if (line == null) {
                line = new HashMap<String, Object>();
                line.put("productId", item.getProductId());
                line.put("merchantId", item.getMerchantId());
                line.put("quantity", item.getQuantity());
                byProduct.put(item.getProductId(), line);
            } else {
                if (!item.getMerchantId().equals(line.get("merchantId"))) {
                    throw TradeException.conflict("order SKU has inconsistent merchants");
                }
                line.put("quantity", Math.addExact((Integer) line.get("quantity"), item.getQuantity()));
            }
        }
        return new ArrayList<Map<String, Object>>(byProduct.values());
    }
    public void refundAfterSale(String afterSaleNo, Long merchantId, List<AfterSaleItem> items) { List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>(); for (AfterSaleItem item : items) { Map<String, Object> line = new HashMap<String, Object>(); line.put("productId", item.getProductId()); line.put("merchantId", merchantId); line.put("quantity", item.getQuantity()); lines.add(line); } Map<String, Object> body = new HashMap<String, Object>(); body.put("afterSaleNo", afterSaleNo); body.put("items", lines); post(baseUrl + "/api/v1/internal/after-sales/inventory/refund", body, "order-after-sale-inventory-refund-" + afterSaleNo); }
    public void exchangeAfterSale(String afterSaleNo, Long merchantId, List<AfterSaleItem> items) { List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>(); for (AfterSaleItem item : items) { Map<String, Object> line = new HashMap<String, Object>(); line.put("productId", item.getProductId()); line.put("merchantId", merchantId); line.put("quantity", item.getQuantity()); lines.add(line); } Map<String, Object> body = new HashMap<String, Object>(); body.put("afterSaleNo", afterSaleNo); body.put("items", lines); post(baseUrl + "/api/v1/internal/after-sales/inventory/exchange", body, "order-after-sale-inventory-exchange-" + afterSaleNo); }

    private void post(String url, Object body, String idempotencyKey) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<Object>(body, internalHeaders(idempotencyKey)), Map.class);
            TradeResponse.requireSuccess(response, "inventory operation");
        } catch (TradeException exception) {
            throw exception;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw TradeException.conflict("inventory operation conflicts with current state");
            }
            throw TradeException.conflict("inventory service is unavailable");
        } catch (Exception exception) {
            throw TradeException.conflict("inventory service is unavailable");
        }
    }

    private HttpHeaders internalHeaders(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TradeHeaders.ACTOR_ID, "1");
        headers.set(TradeHeaders.ACTOR_TYPE, ActorType.SYSTEM.name());
        headers.set(TradeHeaders.IDEMPOTENCY_KEY, idempotencyKey);
        headers.set(InternalServiceGuard.HEADER, internalToken);
        return headers;
    }
}
