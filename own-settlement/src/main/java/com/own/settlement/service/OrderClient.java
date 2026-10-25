package com.own.settlement.service;

import com.own.face.trade.config.ServiceEndpoint;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeResponse;
import com.own.face.trade.OrderRecoverySnapshot;
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
import org.springframework.web.client.HttpClientErrorException;

@Component
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalToken;
    public OrderClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken, @Value("${trade.services.order-base-url}") String baseUrl) { this.restTemplate = restTemplate; this.baseUrl = ServiceEndpoint.requireRoot(baseUrl); this.internalToken = internalToken; }

    @SuppressWarnings("unchecked")
    public BigDecimal payableAmount(String orderNo, Long buyerId) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/orders/" + orderNo
                            + "/payable-amount?buyerId=" + buyerId,
                    HttpMethod.GET, new HttpEntity<Void>(internalHeaders("payment-amount-" + orderNo)), Map.class);
            Map<?, ?> body = TradeResponse.requireSuccess(response, "order payable amount");
            Object amount = body.get("data");
            if (amount == null) throw TradeException.notFound("order was not found");
            return new BigDecimal(String.valueOf(amount));
        } catch (TradeException exception) { throw exception;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404) throw TradeException.notFound("order was not found");
            if (exception.getStatusCode().value() == 409) throw TradeException.conflict("order is not awaiting payment");
            throw TradeException.conflict("order service is unavailable");
        } catch (Exception exception) { throw TradeException.conflict("order service is unavailable"); }
    }

    public void paymentSucceeded(String orderNo) { callback(orderNo, "payment-succeeded"); }
    public void refundSucceeded(String orderNo) { callback(orderNo, "refund-succeeded"); }
    public OrderRecoverySnapshot recoverySnapshot(String orderNo) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/orders/" + orderNo + "/recovery-snapshot",
                    HttpMethod.GET, new HttpEntity<Void>(internalHeaders("recovery-snapshot-" + orderNo)), Map.class);
            Object data = TradeResponse.requireSuccess(response, "order recovery snapshot").get("data");
            if (!(data instanceof Map<?, ?> row) || !orderNo.equals(row.get("orderNo"))
                    || !(row.get("status") instanceof String) || !(row.get("paymentRecoveryStatus") instanceof String)) {
                throw TradeException.conflict("invalid order recovery snapshot");
            }
            return new OrderRecoverySnapshot(orderNo, Long.valueOf(String.valueOf(row.get("buyerId"))),
                    new BigDecimal(String.valueOf(row.get("payableAmount"))), (String) row.get("status"),
                    (String) row.get("paymentRecoveryStatus"));
        } catch (TradeException exception) { throw exception;
        } catch (Exception exception) {
            TradeException failure = TradeException.conflict("order recovery snapshot is unavailable");
            if (exception instanceof org.springframework.web.client.ResourceAccessException) failure.initCause(exception);
            throw failure;
        }
    }
    @SuppressWarnings("unchecked")
    public List<MerchantSettlementLine> settlementLines(String orderNo) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/orders/" + orderNo + "/settlement-lines",
                    HttpMethod.GET, new HttpEntity<Void>(internalHeaders("settlement-lines-" + orderNo)), Map.class);
            Map<?, ?> body = TradeResponse.requireSuccess(response, "order settlement lines"); Object data = body.get("data");
            if (!(data instanceof List)) throw TradeException.conflict("order settlement lines are unavailable");
            List<MerchantSettlementLine> result = new ArrayList<MerchantSettlementLine>();
            for (Object raw : (List) data) { if (!(raw instanceof Map)) throw TradeException.conflict("invalid order settlement line"); Map line = (Map) raw; result.add(new MerchantSettlementLine(Long.valueOf(String.valueOf(line.get("merchantId"))), new BigDecimal(String.valueOf(line.get("grossAmount"))))); }
            return result;
        } catch (TradeException exception) { throw exception; }
        catch (Exception exception) { throw TradeException.conflict("order service is unavailable"); }
    }
    public void afterSaleRefundSucceeded(String afterSaleNo) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/after-sales/" + afterSaleNo + "/refund-succeeded",
                    HttpMethod.POST, new HttpEntity<Void>(internalHeaders("settlement-after-sale-" + afterSaleNo)), Map.class);
            TradeResponse.requireSuccess(response, "order after-sale refund callback");
        } catch (TradeException exception) { throw exception;
        } catch (Exception exception) { throw TradeException.conflict("order service callback failed"); }
    }

    private void callback(String orderNo, String action) {
        try {
            HttpHeaders headers = internalHeaders("settlement-" + action + "-" + orderNo);
            ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/api/v1/internal/orders/" + orderNo + "/" + action,
                    HttpMethod.POST, new HttpEntity<Void>(headers), Map.class);
            TradeResponse.requireSuccess(response, "order " + action + " callback");
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.conflict("order service callback failed");
        }
    }
    private HttpHeaders internalHeaders(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TradeHeaders.ACTOR_ID, "1"); headers.set(TradeHeaders.ACTOR_TYPE, ActorType.SYSTEM.name()); headers.set(TradeHeaders.IDEMPOTENCY_KEY, key);
        headers.set(InternalServiceGuard.HEADER, internalToken);
        return headers;
    }
}
