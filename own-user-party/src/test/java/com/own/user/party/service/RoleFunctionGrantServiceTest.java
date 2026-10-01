package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.FunDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.Fun;
import com.own.user.party.dao.domain.Role;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.transaction.annotation.Transactional;

public class RoleFunctionGrantServiceTest {
    @Test
    void validatesTheWholeBatchBeforeWritingAnyGrant() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findById(7L)).thenReturn(Optional.of(new Role()));
        when(functions.findById(3L)).thenReturn(Optional.of(new Fun()));
        assertThrows(TradeException.class,
                () -> new RoleFunctionGrantService(roles, functions).grant(7L, Arrays.asList(3L, 9L)));
        verify(roles, never()).createRelationShipRoleAndFun(anyLong(), anyLong());
    }

    @Test
    void grantsUseAnExplicitGraphTransaction() throws Exception {
        Transactional transaction = RoleFunctionGrantService.class.getMethod("grant", Long.class, List.class)
                .getAnnotation(Transactional.class);
        org.junit.jupiter.api.Assertions.assertNotNull(transaction);
        assertEquals("neo4jTransactionManager", transaction.transactionManager());
    }

    @Test
    public void grantsDistinctExistingFunctionsOnlyOnce() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findById(7L)).thenReturn(Optional.of(new Role()));
        when(functions.findById(3L)).thenReturn(Optional.of(new Fun())); when(functions.findById(4L)).thenReturn(Optional.of(new Fun()));
        List<Long> result = new RoleFunctionGrantService(roles, functions).grant(7L, Arrays.asList(3L, 3L, 4L));
        assertEquals(Arrays.asList(3L, 4L), result);
        verify(roles).createRelationShipRoleAndFun(7L, 3L);
        verify(roles).createRelationShipRoleAndFun(7L, 4L);
    }

    @Test
    public void rejectsMissingFunctionBeforeCreatingDanglingEdge() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findById(7L)).thenReturn(Optional.of(new Role()));
        try {
            new RoleFunctionGrantService(roles, functions).grant(7L, Arrays.asList(9L));
            fail("missing function must be rejected");
        } catch (TradeException expected) { assertEquals(404, expected.getStatus()); }
    }
}
