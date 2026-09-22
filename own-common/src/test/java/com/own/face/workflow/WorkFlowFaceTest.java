package com.own.face.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WorkFlowFaceTest {

    @Test
    public void returnsSubmittedWorkflowAfterPut() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WorkFlowFace face = new WorkFlowFace();
        ReflectionTestUtils.setField(face, "restTemplate", restTemplate);
        Map<String, Object> workflow = new HashMap<String, Object>();
        workflow.put("processId", Long.valueOf(7));

        Object result = face.putWorkFlow(workflow);

        assertSame(workflow, result);
        verify(restTemplate).put(anyString(), anyMap());
    }
}
