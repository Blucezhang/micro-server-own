package com.own.face.trade.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.util.Resp;
import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TradeIdempotencyCompletionFailureTest {
    private final TradeIdempotencyStore store = mock(TradeIdempotencyStore.class);
    private final TradeIdempotencyAspect aspect = new TradeIdempotencyAspect(store, new ObjectMapper(), "product", 24);

    @Test
    void responsePersistenceFailureDoesNotForgetSuccessfulBusinessWrite() throws Throwable {
        ProceedingJoinPoint invocation = invocation();
        Resp response = new Resp("created", 201, "created");
        when(invocation.proceed()).thenReturn(response);
        doThrow(new IllegalStateException("response store unavailable")).when(store).complete(null, response);
        assertThrows(IllegalStateException.class, () -> aspect.protect(invocation));
        verify(store, never()).abandon(any());
        verify(invocation, times(1)).proceed();
    }

    @Test
    void businessFailureStillReleasesTheUncompletedAttempt() throws Throwable {
        ProceedingJoinPoint invocation = invocation();
        when(invocation.proceed()).thenThrow(new IllegalArgumentException("invalid product"));
        assertThrows(IllegalArgumentException.class, () -> aspect.protect(invocation));
        verify(store).abandon(null);
        verify(store, never()).complete(any(), any());
    }

    private ProceedingJoinPoint invocation() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/product/api/v1/merchant/products");
        request.addHeader("X-Actor-Id", "7"); request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", "create-1");
        ProceedingJoinPoint invocation = mock(ProceedingJoinPoint.class);
        when(invocation.getArgs()).thenReturn(new Object[] { "command", request });
        when(store.begin(anyString(), any(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(new IdempotencyRecord("product", 7L, "MERCHANT", request.getRequestURI(),
                        "create-1", "hash", new Date(System.currentTimeMillis() + 60_000)));
        return invocation;
    }
}
