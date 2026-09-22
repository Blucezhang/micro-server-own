package com.own.send.server.service;

import com.own.send.server.dao.RdbBaseDao;
import com.own.send.server.domain.Sms;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RdbSvcTest {

    @Test
    public void updateBySaveRejectsUnknownIdentifier() {
        RdbBaseDao dao = mock(RdbBaseDao.class);
        Sms sms = new Sms();
        sms.setId(9);
        when(dao.find(sms, 9)).thenReturn(null);
        RdbSvc service = new RdbSvc();
        service.setBaseDao(dao);

        assertThrows(EntityNotFoundException.class, () -> service.save(sms));
    }
}
