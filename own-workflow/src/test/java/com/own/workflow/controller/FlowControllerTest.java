package com.own.workflow.controller;

import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.workflow.service.FlowSvc;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FlowControllerTest {
    @Test
    public void invalidProcessIdDoesNotReachWorkflowQuery() {
        try {
            controller(mock(FlowSvc.class)).getTransInfor(0L, 1);
            fail("invalid process id must be rejected");
        } catch (TradeException expected) { assertEquals(422, expected.getStatus()); }
    }

    @Test
    public void validWorkflowDetailUsesExistingResponseShape() {
        FlowSvc service = mock(FlowSvc.class);
        when(service.queryProcessMater(anyMap())).thenReturn(Collections.emptyList());
        Resp response = controller(service).getTransInfor(7L, 2);
        assertEquals(Collections.emptyList(), ((Map) response.getData()).get("TransListByProcessId"));
    }

    private FlowController controller(FlowSvc service) {
        FlowController controller = new FlowController();
        ReflectionTestUtils.setField(controller, "flowSvc", service);
        return controller;
    }
}
