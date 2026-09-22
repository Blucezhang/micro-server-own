package com.own.face.ems;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmsFaceTest {
    @Test
    public void messageFaceAcceptsOnlyExplicitServiceSuccess() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class), anyMap()))
                .thenReturn("{\"data\":\"success\",\"status\":200,\"message\":\"success\"}");
        EmsFace face = face(restTemplate);
        assertTrue(face.sendMsg("13800138000", "hello"));
    }

    @Test
    public void messageFaceDoesNotTreatFailedPayloadAsDeliverySuccess() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class), anyMap()))
                .thenReturn("{\"data\":\"failed\",\"status\":200,\"message\":\"success\"}");
        EmsFace face = face(restTemplate);
        assertFalse(face.sendEmail("buyer@example.test", "hello", "body"));
    }

    private EmsFace face(RestTemplate restTemplate) {
        EmsFace face = new EmsFace();
        ReflectionTestUtils.setField(face, "restTemplate", restTemplate);
        return face;
    }
}
