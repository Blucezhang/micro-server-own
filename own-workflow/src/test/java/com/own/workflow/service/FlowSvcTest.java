package com.own.workflow.service;

import org.junit.Test;

import java.util.Collections;

public class FlowSvcTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingProcessIdBeforeDereferencingIt() {
        new FlowSvc().doFlow(Collections.singletonMap("funName", "approve"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingFunctionNameBeforeDereferencingIt() {
        new FlowSvc().doFlow(Collections.singletonMap("processId", 1L));
    }
}
