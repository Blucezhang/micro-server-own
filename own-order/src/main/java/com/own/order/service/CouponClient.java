package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.face.trade.TradeResponse;
import com.own.face.security.InternalServiceGuard;
import com.own.order.dto.CouponUse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

@Component
public class CouponClient {

    private final RestTemplate restTemplate;
    private final String internalToken;
    @Value("${trade.saga.reservation-ttl-minutes:15}") private int reservationTtlMinutes = 15;

    public CouponClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }

    @SuppressWarnings("unchecked")
    public CouponReservation reserve(String orderNo, Long buyerId, BigDecimal parentAmount,
                                     Map<Long, BigDecimal> merchantAmounts, List<CouponUse> coupons) {
        if (coupons == null || coupons.isEmpty()) {
            return new CouponReservation(BigDecimal.ZERO, new HashMap<Long, BigDecimal>());
        }
        Map<String, Object> command = command(orderNo, buyerId, parentAmount, merchantAmounts, coupons);
        command.put("ttlMinutes", reservationTtlMinutes);
        try {
            ResponseEntity<Map> response = restTemplate.exchange("http://own-promotion/sale/api/v1/coupons/internal/reservations",
                    HttpMethod.POST, new HttpEntity<Map<String, Object>>(command, headers("order-coupon-reserve-" + orderNo)), Map.class);
            return reservation(response);
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.conflict("promotion service is unavailable");
        }
    }

    @SuppressWarnings("unchecked")
    public CouponReservation preview(Long buyerId, BigDecimal parentAmount, Map<Long, BigDecimal> merchantAmounts, List<CouponUse> coupons) {
        if (coupons == null || coupons.isEmpty()) return new CouponReservation(BigDecimal.ZERO, new HashMap<Long, BigDecimal>());
        try {
            ResponseEntity<Map> response = restTemplate.exchange("http://own-promotion/sale/api/v1/coupons/internal/quotes",
                    HttpMethod.POST, new HttpEntity<Map<String, Object>>(command(null, buyerId, parentAmount, merchantAmounts, coupons), headers("order-coupon-preview")), Map.class);
            return reservation(response);
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.conflict("promotion service is unavailable");
        }
    }

    private Map<String, Object> command(String orderNo, Long buyerId, BigDecimal parentAmount,
                                        Map<Long, BigDecimal> merchantAmounts, List<CouponUse> coupons) {
        Map<String, Object> command = new HashMap<String, Object>();
        command.put("orderNo", orderNo);
        command.put("buyerId", buyerId);
        command.put("parentAmount", parentAmount);
        command.put("merchantAmounts", merchantAmounts);
        List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
        for (CouponUse coupon : coupons) {
            Map<String, Object> value = new HashMap<String, Object>();
            value.put("couponNo", coupon.getCouponNo());
            value.put("merchantId", coupon.getMerchantId());
            value.put("applicableAmount", coupon.getApplicableAmount());
            values.add(value);
        }
        command.put("coupons", values);
        return command;
    }

    @SuppressWarnings("unchecked")
    private CouponReservation reservation(ResponseEntity<Map> response) {
        Map<?, ?> body = TradeResponse.requireSuccess(response, "coupon calculation");
        Map data = !(body.get("data") instanceof Map) ? null : (Map) body.get("data");
        if (data == null || data.get("totalDiscount") == null) throw TradeException.conflict("coupon calculation failed");
        Map<Long, BigDecimal> discounts = new HashMap<Long, BigDecimal>();
        Object discountValues = data.get("merchantDiscounts");
        if (discountValues instanceof Map) for (Object entryObject : ((Map) discountValues).entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            discounts.put(Long.valueOf(String.valueOf(entry.getKey())), new BigDecimal(String.valueOf(entry.getValue())));
        }
        return new CouponReservation(new BigDecimal(String.valueOf(data.get("totalDiscount"))), discounts);
    }

    public void consume(String orderNo, BigDecimal expectedDiscount) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("orderNo", orderNo);
        body.put("expectedDiscount", expectedDiscount);
        action("http://own-promotion/sale/api/v1/coupons/internal/orders/consume", body, "order-coupon-consume-" + orderNo);
    }
    public void reconcileConsume(String orderNo, Long buyerId, BigDecimal expectedDiscount) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange("http://own-promotion/sale/api/v1/coupons/internal/orders/" + orderNo + "/state",
                    HttpMethod.GET, new HttpEntity<Void>(headers("coupon-state-" + orderNo)), Map.class);
            Object data = TradeResponse.requireSuccess(response, "coupon recovery state").get("data");
            if (!(data instanceof List<?> rows)) throw TradeException.conflict("invalid coupon recovery state");
            BigDecimal total = BigDecimal.ZERO;
            boolean consumed = true;
            java.util.Set<Object> seen = new java.util.HashSet<>();
            for (Object raw : rows) {
                if (!(raw instanceof Map<?, ?> row) || !orderNo.equals(row.get("orderNo"))
                        || !String.valueOf(buyerId).equals(String.valueOf(row.get("buyerId")))
                        || row.get("couponNo") == null || !seen.add(row.get("couponNo"))
                        || !("USED".equals(row.get("status")) || "RESERVED".equals(row.get("status")))) {
                    throw TradeException.conflict("coupon recovery state does not match order");
                }
                BigDecimal amount = new BigDecimal(String.valueOf(row.get("discountAmount")));
                if (amount.signum() < 0 || amount.stripTrailingZeros().scale() > 2) throw TradeException.conflict("invalid coupon recovery amount");
                total = total.add(amount);
                consumed &= "USED".equals(row.get("status"));
            }
            if (total.compareTo(expectedDiscount) != 0) throw TradeException.conflict("coupon recovery amount does not match order");
            if (!consumed) consume(orderNo, expectedDiscount);
        } catch (TradeException exception) { throw exception;
        } catch (Exception exception) {
            TradeException failure = TradeException.conflict("coupon recovery state is unavailable");
            if (exception instanceof org.springframework.web.client.ResourceAccessException) failure.initCause(exception);
            throw failure;
        }
    }
    public void release(String orderNo) { action("http://own-promotion/sale/api/v1/coupons/internal/orders/release", orderNo, "order-coupon-release-" + orderNo); }

    private void action(String url, Object body, String idempotencyKey) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<Object>(body, headers(idempotencyKey)), Map.class);
            TradeResponse.requireSuccess(response, "coupon operation");
        } catch (TradeException exception) {
            throw exception;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw TradeException.conflict("coupon reservation no longer matches order");
            }
            throw TradeException.conflict("promotion service is unavailable");
        } catch (Exception exception) {
            throw TradeException.conflict("promotion service is unavailable");
        }
    }

    private HttpHeaders headers(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", "1"); headers.set("X-Actor-Type", "SYSTEM");
        headers.set("Idempotency-Key", idempotencyKey);
        headers.set(InternalServiceGuard.HEADER, internalToken);
        return headers;
    }

    public static class CouponReservation {
        private final BigDecimal totalDiscount;
        private final Map<Long, BigDecimal> merchantDiscounts;
        CouponReservation(BigDecimal totalDiscount, Map<Long, BigDecimal> merchantDiscounts) {
            this.totalDiscount = totalDiscount;
            this.merchantDiscounts = merchantDiscounts;
        }
        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public Map<Long, BigDecimal> getMerchantDiscounts() { return merchantDiscounts; }
    }
}
