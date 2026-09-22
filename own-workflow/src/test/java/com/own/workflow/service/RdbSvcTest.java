package com.own.workflow.service;

import com.own.workflow.dao.RdbBaseDao;
import com.own.workflow.domain.BizBusinessFlowContext;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RdbSvcTest {

    @Test
    public void updateBySaveRejectsUnknownIdentifier() {
        RdbBaseDao dao = mock(RdbBaseDao.class);
        BizBusinessFlowContext context = new BizBusinessFlowContext();
        context.setBusinessflowId(9);
        when(dao.find(context, 9)).thenReturn(null);
        RdbSvc service = new RdbSvc();
        service.setBaseDao(dao);

        assertThrows(EntityNotFoundException.class, () -> service.save(context));
    }
}
