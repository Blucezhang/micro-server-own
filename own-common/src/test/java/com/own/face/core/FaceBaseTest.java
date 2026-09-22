package com.own.face.core;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

public class FaceBaseTest {

    @Test
    public void postUsesJsonEntityAndKeepsStringPayloadsRaw() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForObject(eq("http://test"), any(HttpEntity.class),
                eq(String.class), anyMap())).thenReturn("ok");
        ExposedFace face = new ExposedFace();
        ReflectionTestUtils.setField(face, "restTemplate", restTemplate);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("content", "a \"quoted\" value");
        assertEquals("ok", face.sendPost(payload));

        ArgumentCaptor<Object> requestCaptor = ArgumentCaptor.forClass(Object.class);
        verify(restTemplate).postForObject(eq("http://test"), requestCaptor.capture(),
                eq(String.class), anyMap());
        HttpEntity<?> entity = (HttpEntity<?>) requestCaptor.getValue();
        assertEquals("{\"content\":\"a \\\"quoted\\\" value\"}", entity.getBody());
    }

    private static class ExposedFace extends FaceBase {
        private String sendPost(Object payload) {
            return post("http://test", payload, String.class, new HashMap<String, Object>());
        }
    }
}
