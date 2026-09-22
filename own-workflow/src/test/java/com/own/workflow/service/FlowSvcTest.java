package com.own.workflow.service;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlowSvcTest {

    @Test
    public void rejectsMissingProcessIdBeforeDereferencingIt() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlowSvc().doFlow(Collections.singletonMap("funName", "approve")));
    }

    @Test
    public void rejectsMissingFunctionNameBeforeDereferencingIt() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlowSvc().doFlow(Collections.singletonMap("processId", 1L)));
    }
}
