package com.own.workflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BizBusinessFlowContextTest {

    @Test
    public void exposesPersistenceIdentifier() {
        BizBusinessFlowContext context = new BizBusinessFlowContext();
        context.setBusinessflowId(17);

        assertEquals(Integer.valueOf(17), context.getObjectId());
    }
}
