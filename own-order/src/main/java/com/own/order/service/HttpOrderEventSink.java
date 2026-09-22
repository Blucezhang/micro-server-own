package com.own.order.service;

import com.own.order.domain.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

/** Optional outbound adapter. Receivers deduplicate by event ID and verify its HMAC signature. */
@Component
@Primary
@ConditionalOnProperty(name = "trade.outbox.rocketmq.enabled", havingValue = "false", matchIfMissing = true)
public class HttpOrderEventSink implements OrderEventSink {
    private static final String HMAC = "HmacSHA256";
    private final RestTemplate restTemplate; private final String webhookUrl; private final String webhookSecret; private final ObjectMapper objectMapper;
    public HttpOrderEventSink(@Qualifier("outboxRestTemplate") RestTemplate restTemplate,
                              @Value("${trade.outbox.webhook-url:}") String webhookUrl,
                              @Value("${trade.outbox.webhook-secret:}") String webhookSecret,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate; this.webhookUrl = webhookUrl; this.webhookSecret = webhookSecret; this.objectMapper = objectMapper;
    }
    public boolean isEnabled() { return hasSecureUrl() && secretIsStrong(); }
    public String target() { return isEnabled() ? "WEBHOOK" : "WEBHOOK_UNCONFIGURED"; }
    public void deliver(OrderEvent event) {
        if (!isEnabled()) throw new IllegalStateException("outbox HTTPS webhook URL and a 32-byte webhook secret are required");
        HttpHeaders headers = new HttpHeaders(); headers.set("X-Order-Event-Id", String.valueOf(event.getId())); headers.set("X-Order-Event-Type", event.getEventType());
        Map<String,Object> body = new LinkedHashMap<String,Object>(); body.put("id", event.getId()); body.put("orderNo", event.getOrderNo()); body.put("subOrderNo", event.getSubOrderNo()); body.put("afterSaleNo", event.getAfterSaleNo()); body.put("eventType", event.getEventType()); body.put("actorId", event.getActorId()); body.put("actorType", event.getActorType()); body.put("payload", event.getPayload()); body.put("createdAt", event.getCreatedAt());
        try {
            String serialized = objectMapper.writeValueAsString(body);
            headers.set("X-Order-Event-Signature", "sha256=" + hex(hmac(serialized)));
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            ResponseEntity<Void> response = restTemplate.postForEntity(webhookUrl, new HttpEntity<String>(serialized, headers), Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw new IllegalStateException("outbox webhook rejected event with " + response.getStatusCode().value());
        } catch (IllegalStateException exception) { throw exception; }
        catch (Exception exception) { throw new IllegalStateException("outbox webhook payload could not be signed", exception); }
    }
    private boolean hasSecureUrl() {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) return false;
        try {
            URI value = new URI(webhookUrl.trim());
            return "https".equalsIgnoreCase(value.getScheme()) && value.getHost() != null && !value.getHost().trim().isEmpty();
        } catch (Exception ignored) { return false; }
    }
    private boolean secretIsStrong() { return webhookSecret != null && webhookSecret.getBytes(StandardCharsets.UTF_8).length >= 32; }
    private byte[] hmac(String value) throws Exception { Mac mac = Mac.getInstance(HMAC); mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC)); return mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); }
    private String hex(byte[] value) { StringBuilder result = new StringBuilder(value.length * 2); for (byte item : value) result.append(String.format("%02x", item & 0xff)); return result.toString(); }
}
