package com.own.workflow.dao;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RdbBaseDaoTest {

    @Test
    public void nativeQueryReturnsEmptyPageInsteadOfNull() {
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery("select ID from biz_state where 1=0")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        RdbBaseDao dao = new RdbBaseDao();
        dao.em = entityManager;

        Page<?> result = dao.findAllByNativeSql(
                "select ID from biz_state where 1=0", null, Collections.<String, Object>emptyMap(), new PageRequest(0, 10));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
