package com.own.face.util.base;

import com.own.face.util.Resp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BaseControllerTest {

    @Test
    public void mapsInvalidArgumentsToBadRequestEnvelope() {
        Resp response = new BaseController().handleInvalidRequest(new IllegalArgumentException("processId is required"));

        assertEquals(Integer.valueOf(400), response.getStatus());
        assertEquals("processId is required", response.getMessage());
        assertNull(response.getData());
    }
}
