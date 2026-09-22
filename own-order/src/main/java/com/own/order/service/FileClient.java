package com.own.order.service;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/** Verifies owned-file metadata; binary downloads stay within the file service. */
@Component
public class FileClient {
    private final RestTemplate restTemplate;
    private final String internalToken;
    public FileClient(RestTemplate restTemplate, @Value("${trade.internal.service-token:}") String internalToken) { this.restTemplate = restTemplate; this.internalToken = internalToken; }

    public void requirePermanentBuyerFile(Long buyerId, String storedName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Actor-Id", "1"); headers.set("X-Actor-Type", "SYSTEM");
            headers.set(InternalServiceGuard.HEADER, internalToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://own-file/file/api/v1/files/internal/buyers/" + buyerId + "/" + storedName,
                    HttpMethod.GET, new HttpEntity<Void>(headers), Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw TradeException.unprocessable("after-sale evidence is unavailable");
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.unprocessable("after-sale evidence is unavailable");
        }
    }
}
