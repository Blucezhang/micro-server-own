package com.own.face.security;

import com.own.face.trade.TradeException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Protects service-to-service endpoints independently of user JWT claims. */
@Component
public class InternalServiceGuard {
    public static final String HEADER = "X-Internal-Service-Token";
    private static final int MINIMUM_TOKEN_BYTES = 32;
    private final String token;
    private final String previousToken;

    public InternalServiceGuard(@Value("${trade.internal.service-token:}") String token,
                                @Value("${trade.internal.previous-service-token:}") String previousToken) {
        this.token = token == null ? "" : token;
        this.previousToken = previousToken == null ? "" : previousToken;
    }
    public void require(HttpServletRequest request) {
        if (!isValidConfiguredToken(token)) throw TradeException.forbidden("internal service token is not configured or is too short");
        String supplied = request == null ? null : request.getHeader(HEADER);
        if (!constantTimeEquals(token, supplied) && !acceptsPreviousToken(supplied)) {
            throw TradeException.forbidden("invalid internal service token");
        }
    }

    private boolean acceptsPreviousToken(String supplied) {
        return isValidConfiguredToken(previousToken) && constantTimeEquals(previousToken, supplied);
    }

    private boolean isValidConfiguredToken(String candidate) {
        return candidate != null && candidate.trim().length() > 0
                && candidate.getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= MINIMUM_TOKEN_BYTES;
    }
    private boolean constantTimeEquals(String expected, String supplied) {
        if (supplied == null) return false; byte[] left = expected.getBytes(java.nio.charset.StandardCharsets.UTF_8); byte[] right = supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(left, right);
    }
}
