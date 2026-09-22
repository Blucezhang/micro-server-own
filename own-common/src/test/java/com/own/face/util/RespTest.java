package com.own.face.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RespTest {

    @Test
    public void wrapsSuccessfulDataWithDefaultStatus() {
        Resp response = new Resp("payload");

        assertEquals("payload", response.getData());
        assertEquals(Integer.valueOf(200), response.getStatus());
        assertEquals("success", response.getMessage());
    }

    @Test
    public void supportsExplicitStatusAndMessage() {
        Resp response = new Resp(null, 422, "invalid request");

        assertNull(response.getData());
        assertEquals(Integer.valueOf(422), response.getStatus());
        assertEquals("invalid request", response.getMessage());
    }
}
