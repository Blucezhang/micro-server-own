package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.FunDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.Fun;
import com.own.user.party.dao.domain.Role;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleFunctionGrantServiceTest {
    @Test
    public void grantsDistinctExistingFunctionsOnlyOnce() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findOne(7L)).thenReturn(new Role());
        when(functions.findOne(3L)).thenReturn(new Fun()); when(functions.findOne(4L)).thenReturn(new Fun());
        List<Long> result = new RoleFunctionGrantService(roles, functions).grant(7L, Arrays.asList(3L, 3L, 4L));
        assertEquals(Arrays.asList(3L, 4L), result);
        verify(roles).createRelationShipRoleAndFun(7L, 3L);
        verify(roles).createRelationShipRoleAndFun(7L, 4L);
    }

    @Test
    public void rejectsMissingFunctionBeforeCreatingDanglingEdge() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findOne(7L)).thenReturn(new Role());
        try {
            new RoleFunctionGrantService(roles, functions).grant(7L, Arrays.asList(9L));
            fail("missing function must be rejected");
        } catch (TradeException expected) { assertEquals(404, expected.getStatus()); }
    }
}
