package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.face.security.InternalServiceGuard;
import com.own.order.domain.AfterSale;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

/** Local-only simulated settlement adapter; it is not a real payment gateway. */
@Component
public class SettlementClient {
    private final RestTemplate restTemplate;
    private final String internalToken;
    public SettlementClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }
    public void refundAfterSale(AfterSale sale) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("afterSaleNo", sale.getAfterSaleNo()); body.put("orderNo", sale.getOrderNo()); body.put("buyerId", sale.getBuyerId()); body.put("amount", sale.getRequestedAmount());
        try {
            ResponseEntity<Map> response = restTemplate.exchange("http://own-settlement/api/v1/payments/internal/after-sales/refund", HttpMethod.POST, new HttpEntity<Object>(body, headers(sale.getAfterSaleNo())), Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw TradeException.conflict("settlement operation failed");
        } catch (TradeException exception) { throw exception; } catch (Exception exception) { throw TradeException.conflict("settlement service is unavailable"); }
    }
    private HttpHeaders headers(String no) { HttpHeaders headers = new HttpHeaders(); headers.set("X-Actor-Id", "1"); headers.set("X-Actor-Type", "SYSTEM"); headers.set("Idempotency-Key", "order-after-sale-" + no); headers.set(InternalServiceGuard.HEADER, internalToken); return headers; }
}
