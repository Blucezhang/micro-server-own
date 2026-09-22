package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.face.security.InternalServiceGuard;
import com.own.order.domain.OrderItem;
import com.own.order.domain.AfterSaleItem;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String internalToken;

    public InventoryClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) {
        this.restTemplate = restTemplate;
        this.internalToken = internalToken;
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
        body.put("ttlMinutes", 15);
        post("http://own-inventory/api/v1/internal/reservations", body,
                "order-inventory-reserve-" + orderNo + "-" + productId + "-" + merchantId);
    }

    public void commit(String orderNo) { post("http://own-inventory/api/v1/internal/orders/" + orderNo + "/inventory/commit", null, "order-inventory-commit-" + orderNo); }
    public void release(String orderNo) { post("http://own-inventory/api/v1/internal/orders/" + orderNo + "/inventory/release", null, "order-inventory-release-" + orderNo); }
    public void refund(String orderNo) { post("http://own-inventory/api/v1/internal/orders/" + orderNo + "/inventory/refund", null, "order-inventory-refund-" + orderNo); }
    public void refundAfterSale(String afterSaleNo, Long merchantId, List<AfterSaleItem> items) { List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>(); for (AfterSaleItem item : items) { Map<String, Object> line = new HashMap<String, Object>(); line.put("productId", item.getProductId()); line.put("merchantId", merchantId); line.put("quantity", item.getQuantity()); lines.add(line); } Map<String, Object> body = new HashMap<String, Object>(); body.put("afterSaleNo", afterSaleNo); body.put("items", lines); post("http://own-inventory/api/v1/internal/after-sales/inventory/refund", body, "order-after-sale-inventory-refund-" + afterSaleNo); }
    public void exchangeAfterSale(String afterSaleNo, Long merchantId, List<AfterSaleItem> items) { List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>(); for (AfterSaleItem item : items) { Map<String, Object> line = new HashMap<String, Object>(); line.put("productId", item.getProductId()); line.put("merchantId", merchantId); line.put("quantity", item.getQuantity()); lines.add(line); } Map<String, Object> body = new HashMap<String, Object>(); body.put("afterSaleNo", afterSaleNo); body.put("items", lines); post("http://own-inventory/api/v1/internal/after-sales/inventory/exchange", body, "order-after-sale-inventory-exchange-" + afterSaleNo); }

    private void post(String url, Object body, String idempotencyKey) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<Object>(body, internalHeaders(idempotencyKey)), Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw TradeException.conflict("inventory operation failed");
            }
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.conflict("inventory service is unavailable");
        }
    }

    private HttpHeaders internalHeaders(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", "1");
        headers.set("X-Actor-Type", "SYSTEM");
        headers.set("Idempotency-Key", idempotencyKey);
        headers.set(InternalServiceGuard.HEADER, internalToken);
        return headers;
    }
}
