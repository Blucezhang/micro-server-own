package com.own.product.service;

import com.own.product.OwnProductApplication;
import com.own.product.dao.ProductDao;
import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

class ProductTransactionContractTest {
    @Test
    void graphRepositoriesUseGraphManagerAndSqlProjectionCommitsIndependently() throws Exception {
        assertEquals("neo4jTransactionManager", OwnProductApplication.class
                .getAnnotation(EnableNeo4jRepositories.class).transactionManagerRef());
        var transaction = ProductPriceAuditSqlWriter.class
                .getMethod("projectAudit", com.own.product.domain.ProductPriceChangeEvent.class)
                .getAnnotation(Transactional.class);
        assertEquals("transactionManager", transaction.transactionManager());
        assertEquals(Propagation.REQUIRES_NEW, transaction.propagation());
    }
    @Test
    void writeLockQueryHasAnExplicitPropertyDependencyAndHiddenCoordinationField() throws Exception {
        String query = ProductDao.class.getMethod("lockProductById", Long.class).getAnnotation(Query.class).value();
        assertTrue(query.contains("priceWriteRevision = coalesce(p.priceWriteRevision, 0) + 1")
                || query.replace(" ", "").contains("priceWriteRevision=coalesce(p.priceWriteRevision,0)+1"));
        assertNotNull(com.own.product.domain.Product.class.getDeclaredField("priceWriteRevision")
                .getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class));
    }
}
