package com.own.settlement.service;

import com.own.face.trade.TradeException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Separate secret for the intentionally public, local channel callback URLs. */
@Component
public class MockPaymentCallbackGuard {
    public static final String HEADER = "X-Mock-Payment-Token";
    private final String token;

    public MockPaymentCallbackGuard(@Value("${trade.payment.mock.callback-token:}") String token) {
        this.token = token == null ? "" : token;
    }

    public void require(HttpServletRequest request) {
        if (token.trim().isEmpty()) throw TradeException.forbidden("mock payment callback token is not configured");
        String supplied = request == null ? null : request.getHeader(HEADER);
        if (supplied == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw TradeException.forbidden("invalid mock payment callback token");
        }
    }
}
