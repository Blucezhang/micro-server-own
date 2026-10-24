package com.own.face.trade;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

class TradeResponseTest {
    @Test
    void preservesSuccessfulDataAndAllIntegerSuccessStatuses() {
        for (int status : new int[] {200, 201, 202, 299}) {
            Map<String, Object> body = Map.of("status", status, "data", "unchanged");
            assertSame(body, TradeResponse.requireSuccess(ResponseEntity.ok(body), "test"));
        }
    }

    @Test
    void rejectsNullResponseEmptyBodyAndMissingStatus() {
        assertEquals(502, assertThrows(TradeException.class, () -> TradeResponse.requireSuccess(null, "test")).getStatus());
        assertEquals(502, assertThrows(TradeException.class,
                () -> TradeResponse.requireSuccess(ResponseEntity.ok().build(), "test")).getStatus());
        assertEquals(502, assertThrows(TradeException.class,
                () -> TradeResponse.requireSuccess(ResponseEntity.ok(Map.of()), "test")).getStatus());
    }

    @Test
    void rejectsStringFractionalAndBooleanStatusInsteadOfCoercingThem() {
        for (Object status : Arrays.asList("200", 200.5, 200.0, true, 200L)) {
            assertEquals(502, assertThrows(TradeException.class,
                    () -> TradeResponse.requireSuccess(ResponseEntity.ok(Map.of("status", status)), "test")).getStatus());
        }
    }

    @Test
    void preservesFailureCodeWithoutLeakingUntrustedMessageOrData() {
        for (int status : new int[] {400, 403, 404, 409, 422, 500, 503}) {
            TradeException error = assertThrows(TradeException.class, () -> TradeResponse.requireSuccess(
                    ResponseEntity.ok(Map.of("status", status, "message", "secret-token", "data", "private-contact")), "test operation"));
            assertEquals(status, error.getStatus());
            assertTrue(error.getMessage().contains("test operation"));
            assertTrue(error.getMessage().contains(String.valueOf(status)));
            assertFalse(error.getMessage().contains("secret-token"));
            assertNull(error.getData());
        }
    }

    @Test
    void redirectsAndOutOfRangeStatusAreInvalidEnvelopes() {
        for (int status : new int[] {-1, 0, 100, 199, 300, 302, 399, 600}) {
            assertEquals(502, assertThrows(TradeException.class, () -> TradeResponse.requireSuccess(
                    ResponseEntity.ok(Map.of("status", status)), "test")).getStatus());
        }
    }

    @Test
    void httpFailureCannotBeMaskedBySuccessfulBusinessStatus() {
        assertEquals(502, assertThrows(TradeException.class, () -> TradeResponse.requireSuccess(
                new ResponseEntity<>(Map.of("status", 200), HttpStatus.SERVICE_UNAVAILABLE), "test")).getStatus());
    }
}
