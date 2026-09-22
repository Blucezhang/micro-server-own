package com.own.workflow.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BizBusinessFlowContextTest {

    @Test
    public void exposesPersistenceIdentifier() {
        BizBusinessFlowContext context = new BizBusinessFlowContext();
        context.setBusinessflowId(17);

        assertEquals(Integer.valueOf(17), context.getObjectId());
    }
}
