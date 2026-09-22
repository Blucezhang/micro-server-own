package com.own.workflow.service;

import com.own.workflow.dao.RdbBaseDao;
import com.own.workflow.domain.BizBusinessFlowContext;
import org.junit.Test;

import javax.persistence.EntityNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RdbSvcTest {

    @Test(expected = EntityNotFoundException.class)
    public void updateBySaveRejectsUnknownIdentifier() {
        RdbBaseDao dao = mock(RdbBaseDao.class);
        BizBusinessFlowContext context = new BizBusinessFlowContext();
        context.setBusinessflowId(9);
        when(dao.find(context, 9)).thenReturn(null);
        RdbSvc service = new RdbSvc();
        service.setBaseDao(dao);

        service.save(context);
    }
}
