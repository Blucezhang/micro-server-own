package com.own.user.party.service;

import com.own.user.party.config.PersistenceTransactionConfig;
import com.own.user.party.UserAndPartyApplication;
import com.own.user.party.dao.FunDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.Fun;
import com.own.user.party.dao.domain.Role;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleGrantTransactionTest {
    @Test
    void secondWriteFailureRollsBackGraphTransactionNotSql() {
        RoleDao roles = mock(RoleDao.class); FunDao functions = mock(FunDao.class);
        when(roles.findById(7L)).thenReturn(Optional.of(new Role()));
        when(functions.findById(any())).thenReturn(Optional.of(new Fun()));
        doThrow(new IllegalStateException("graph write failed")).when(roles).createRelationShipRoleAndFun(7L, 4L);
        PlatformTransactionManager graph = mock(PlatformTransactionManager.class);
        PlatformTransactionManager sql = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(graph.getTransaction(any())).thenReturn(status);
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("neo4jTransactionManager", graph);
        beans.registerSingleton("transactionManager", sql);
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        interceptor.setBeanFactory(beans);
        ProxyFactory proxy = new ProxyFactory(new RoleFunctionGrantService(roles, functions));
        proxy.addAdvice(interceptor);
        RoleFunctionGrantService service = (RoleFunctionGrantService) proxy.getProxy();
        assertThrows(IllegalStateException.class, () -> service.grant(7L, List.of(3L, 4L)));
        verify(graph).rollback(status);
        verify(graph, never()).commit(any());
        verifyNoInteractions(sql);
    }

    @Test
    void graphRepositoriesAndJpaResolveTheirOwnManagers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(EntityManagerFactory.class, () -> mock(EntityManagerFactory.class));
            context.registerBean(Driver.class, () -> mock(Driver.class));
            context.registerBean(DatabaseSelectionProvider.class, DatabaseSelectionProvider::getDefaultSelectionProvider);
            context.register(PersistenceTransactionConfig.class);
            context.refresh();
            assertInstanceOf(JpaTransactionManager.class, context.getBean(PlatformTransactionManager.class));
            assertInstanceOf(org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager.class,
                    context.getBean("neo4jTransactionManager"));
            assertEquals("neo4jTransactionManager", UserAndPartyApplication.class
                    .getAnnotation(EnableNeo4jRepositories.class).transactionManagerRef());
        }
    }
}
