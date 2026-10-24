package com.own.face.trade;

import java.util.Map;
import org.springframework.http.ResponseEntity;

/** Validates the Resp envelope owned by own-common before a trade caller advances. */
public final class TradeResponse {
    private TradeResponse() { }

    public static Map<?, ?> requireSuccess(ResponseEntity<? extends Map> response, String operation) {
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new TradeException(502, operation + " did not return HTTP success");
        }
        Map<?, ?> body = response.getBody();
        Object rawStatus = body == null ? null : body.get("status");
        if (!(rawStatus instanceof Integer)) {
            throw new TradeException(502, operation + " returned an invalid response envelope");
        }
        int status = (Integer) rawStatus;
        if (status < 200 || status >= 300) {
            int failureStatus = status >= 400 && status < 600 ? status : 502;
            throw new TradeException(failureStatus, operation + " rejected: downstream status " + status);
        }
        return body;
    }
}
