package com.own.order.service;

import com.own.face.trade.TradeException;
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
import org.springframework.beans.factory.annotation.Value;

@Component
public class CouponClient {

    private final RestTemplate restTemplate;
    private final String internalToken;

    public CouponClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }

    @SuppressWarnings("unchecked")
    public CouponReservation reserve(String orderNo, Long buyerId, BigDecimal parentAmount,
                                     Map<Long, BigDecimal> merchantAmounts, List<CouponUse> coupons) {
        if (coupons == null || coupons.isEmpty()) {
            return new CouponReservation(BigDecimal.ZERO, new HashMap<Long, BigDecimal>());
        }
        Map<String, Object> command = command(orderNo, buyerId, parentAmount, merchantAmounts, coupons);
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
        Map body = response.getBody();
        Map data = body == null || !(body.get("data") instanceof Map) ? null : (Map) body.get("data");
        if (data == null || data.get("totalDiscount") == null) throw TradeException.conflict("coupon calculation failed");
        Map<Long, BigDecimal> discounts = new HashMap<Long, BigDecimal>();
        Object discountValues = data.get("merchantDiscounts");
        if (discountValues instanceof Map) for (Object entryObject : ((Map) discountValues).entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            discounts.put(Long.valueOf(String.valueOf(entry.getKey())), new BigDecimal(String.valueOf(entry.getValue())));
        }
        return new CouponReservation(new BigDecimal(String.valueOf(data.get("totalDiscount"))), discounts);
    }

    public void consume(String orderNo) { action("http://own-promotion/sale/api/v1/coupons/internal/orders/consume", orderNo, "order-coupon-consume-" + orderNo); }
    public void release(String orderNo) { action("http://own-promotion/sale/api/v1/coupons/internal/orders/release", orderNo, "order-coupon-release-" + orderNo); }

    private void action(String url, String orderNo, String idempotencyKey) {
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<String>(orderNo, headers(idempotencyKey)), Map.class);
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
