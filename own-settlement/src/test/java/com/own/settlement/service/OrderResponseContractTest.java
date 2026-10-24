package com.own.settlement.service;

import com.own.face.trade.TradeException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderResponseContractTest {
    @Test
    void allOrderCallbacksRejectFailedBusinessStatusInsideHttpSuccess() {
        OrderClient client = client(Map.of("status", 409, "data", "ORD-1"), HttpStatus.OK);
        assertThrows(TradeException.class, () -> client.paymentSucceeded("ORD-1"));
        assertThrows(TradeException.class, () -> client.refundSucceeded("ORD-1"));
        assertThrows(TradeException.class, () -> client.afterSaleRefundSucceeded("AS-1"));
    }

    @Test
    void allOrderCallbacksRejectMissingOrMalformedAcknowledgement() {
        for (Map<?, ?> body : List.of(Map.of(), Map.of("status", "200"), Map.of("status", 200.5))) {
            OrderClient client = client(body, HttpStatus.OK);
            assertThrows(TradeException.class, () -> client.paymentSucceeded("ORD-1"));
            assertThrows(TradeException.class, () -> client.refundSucceeded("ORD-1"));
            assertThrows(TradeException.class, () -> client.afterSaleRefundSucceeded("AS-1"));
        }
    }

    @Test
    void payableAmountRejectsFailureEvenWhenDataLooksLikeAValidAmount() {
        OrderClient client = client(Map.of("status", 409, "data", "10.00"), HttpStatus.OK);
        assertThrows(TradeException.class, () -> client.payableAmount("ORD-1", 1L));
    }

    @Test
    void settlementLinesRejectFailureEvenWhenDataLooksLikeValidMerchantLines() {
        OrderClient client = client(Map.of("status", 409,
                "data", List.of(Map.of("merchantId", 2, "grossAmount", "10.00"))), HttpStatus.OK);
        assertThrows(TradeException.class, () -> client.settlementLines("ORD-1"));
    }

    @Test
    void unsuccessfulHttpResponseNeverCountsAsOrderCallbackSuccess() {
        OrderClient client = client(Map.of("status", 200), HttpStatus.SERVICE_UNAVAILABLE);
        assertThrows(TradeException.class, () -> client.paymentSucceeded("ORD-1"));
        assertThrows(TradeException.class, () -> client.refundSucceeded("ORD-1"));
        assertThrows(TradeException.class, () -> client.afterSaleRefundSucceeded("AS-1"));
    }

    @Test
    void properSuccessPreservesExistingDataAndCallbacks() {
        OrderClient amountClient = client(Map.of("status", 200, "data", "10.00"), HttpStatus.OK);
        assertEquals(new BigDecimal("10.00"), amountClient.payableAmount("ORD-1", 1L));
        OrderClient callbackClient = client(Map.of("status", 201), HttpStatus.CREATED);
        assertDoesNotThrow(() -> callbackClient.paymentSucceeded("ORD-1"));
        assertDoesNotThrow(() -> callbackClient.refundSucceeded("ORD-1"));
        assertDoesNotThrow(() -> callbackClient.afterSaleRefundSucceeded("AS-1"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderClient client(Map body, HttpStatus status) {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(body, status));
        return new OrderClient(rest, "test-internal-token");
    }
}
