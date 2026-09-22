package com.own.order.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.order.domain.OrderEvent;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderTimelineEventTest {
    @Test
    public void publicTimelineNeverExposesOutboxOrActorInternals() throws Exception {
        OrderEvent event = new OrderEvent("ORD-1", "SUB-1", "AS-1", "AFTER_SALE_CREATED", 99L, "SYSTEM", "internal detail");
        ReflectionTestUtils.setField(event, "deliveryStatus", "FAILED");
        ReflectionTestUtils.setField(event, "lastDeliveryError", "webhook secret failure");

        String json = new ObjectMapper().writeValueAsString(OrderTimelineEvent.from(event));

        Assert.assertTrue(json.contains("AFTER_SALE_CREATED"));
        Assert.assertFalse(json.contains("internal detail"));
        Assert.assertFalse(json.contains("webhook secret failure"));
        Assert.assertFalse(json.contains("deliveryStatus"));
        Assert.assertFalse(json.contains("actorId"));
    }
}
