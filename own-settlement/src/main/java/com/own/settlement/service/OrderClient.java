package com.own.settlement.service;

import com.own.face.trade.TradeException;
import com.own.face.security.InternalServiceGuard;
import com.own.settlement.dto.MerchantSettlementLine;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String internalToken;
    public OrderClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }

    @SuppressWarnings("unchecked")
    public BigDecimal payableAmount(String orderNo, Long buyerId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Actor-Id", String.valueOf(buyerId)); headers.set("X-Actor-Type", "BUYER");
            ResponseEntity<Map> response = restTemplate.exchange("http://own-order/api/v1/orders/" + orderNo,
                    HttpMethod.GET, new HttpEntity<Void>(headers), Map.class);
            Map body = response.getBody();
            Map data = body == null || !(body.get("data") instanceof Map) ? null : (Map) body.get("data");
            if (data == null || data.get("payableAmount") == null) throw TradeException.notFound("order was not found");
            return new BigDecimal(String.valueOf(data.get("payableAmount")));
        } catch (TradeException exception) { throw exception;
        } catch (Exception exception) { throw TradeException.conflict("order service is unavailable"); }
    }

    public void paymentSucceeded(String orderNo) { callback(orderNo, "payment-succeeded"); }
    public void refundSucceeded(String orderNo) { callback(orderNo, "refund-succeeded"); }
    @SuppressWarnings("unchecked")
    public List<MerchantSettlementLine> settlementLines(String orderNo) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange("http://own-order/api/v1/internal/orders/" + orderNo + "/settlement-lines",
                    HttpMethod.GET, new HttpEntity<Void>(internalHeaders("settlement-lines-" + orderNo)), Map.class);
            Map body = response.getBody(); Object data = body == null ? null : body.get("data");
            if (!(data instanceof List)) throw TradeException.conflict("order settlement lines are unavailable");
            List<MerchantSettlementLine> result = new ArrayList<MerchantSettlementLine>();
            for (Object raw : (List) data) { if (!(raw instanceof Map)) throw TradeException.conflict("invalid order settlement line"); Map line = (Map) raw; result.add(new MerchantSettlementLine(Long.valueOf(String.valueOf(line.get("merchantId"))), new BigDecimal(String.valueOf(line.get("grossAmount"))))); }
            return result;
        } catch (TradeException exception) { throw exception; }
        catch (Exception exception) { throw TradeException.conflict("order service is unavailable"); }
    }
    public void afterSaleRefundSucceeded(String afterSaleNo) {
        try {
            restTemplate.exchange("http://own-order/api/v1/internal/after-sales/" + afterSaleNo + "/refund-succeeded",
                    HttpMethod.POST, new HttpEntity<Void>(internalHeaders("settlement-after-sale-" + afterSaleNo)), Map.class);
        } catch (Exception exception) { throw TradeException.conflict("order service callback failed"); }
    }

    private void callback(String orderNo, String action) {
        try {
            HttpHeaders headers = internalHeaders("settlement-" + action + "-" + orderNo);
            restTemplate.exchange("http://own-order/api/v1/internal/orders/" + orderNo + "/" + action,
                    HttpMethod.POST, new HttpEntity<Void>(headers), Map.class);
        } catch (Exception exception) {
            throw TradeException.conflict("order service callback failed");
        }
    }
    private HttpHeaders internalHeaders(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", "1"); headers.set("X-Actor-Type", "SYSTEM"); headers.set("Idempotency-Key", key);
        headers.set(InternalServiceGuard.HEADER, internalToken);
        return headers;
    }
}
