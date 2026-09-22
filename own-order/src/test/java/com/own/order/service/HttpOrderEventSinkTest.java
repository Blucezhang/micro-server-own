package com.own.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.order.domain.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

public class HttpOrderEventSinkTest {
    @Test
    @SuppressWarnings("unchecked")
    public void sendsStableEventShapeToConfiguredWebhook() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForEntity(eq("https://example.test/events"), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.ACCEPTED));
        HttpOrderEventSink sink = new HttpOrderEventSink(rest, "https://example.test/events", "01234567890123456789012345678901", new ObjectMapper());
        OrderEvent event = new OrderEvent("ORD-1", "SUB-1", "ORDER_CREATED", 1L, "BUYER", "pending");
        sink.deliver(event);
        assertTrue(sink.isEnabled()); assertEquals("WEBHOOK", sink.target());
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).postForEntity(eq("https://example.test/events"), request.capture(), eq(Void.class));
        assertTrue(request.getValue().getHeaders().getFirst("X-Order-Event-Signature").matches("sha256=[0-9a-f]{64}"));
        assertTrue(String.valueOf(request.getValue().getBody()).contains("ORDER_CREATED"));
    }

    @Test
    public void webhookWithoutStrongSecretRemainsDisabledInsteadOfSendingUnsignedEvent() {
        HttpOrderEventSink sink = new HttpOrderEventSink(mock(RestTemplate.class), "https://example.test/events", "short", new ObjectMapper());
        assertEquals(false, sink.isEnabled()); assertEquals("WEBHOOK_UNCONFIGURED", sink.target());
    }

    @Test
    public void plainHttpWebhookRemainsDisabledEvenWithStrongSecret() {
        HttpOrderEventSink sink = new HttpOrderEventSink(mock(RestTemplate.class), "http://example.test/events", "01234567890123456789012345678901", new ObjectMapper());
        assertEquals(false, sink.isEnabled()); assertEquals("WEBHOOK_UNCONFIGURED", sink.target());
    }
}
