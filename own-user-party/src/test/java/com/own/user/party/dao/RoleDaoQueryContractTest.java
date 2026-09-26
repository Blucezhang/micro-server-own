package com.own.user.party.dao;

import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleDaoQueryContractTest {
    @Test
    void roleAndFunctionIdsBindToTheirOwnGraphLabels() throws Exception {
        var method = RoleDao.class.getMethod("createRelationShipRoleAndFun", Long.class, Long.class);
        String query = method.getAnnotation(Query.class).value();
        assertEquals("roleId", method.getParameters()[0].getAnnotation(Param.class).value());
        assertEquals("functionId", method.getParameters()[1].getAnnotation(Param.class).value());
        assertTrue(query.contains("id(role) = $roleId"));
        assertTrue(query.contains("id(function) = $functionId"));
        assertTrue(query.contains("(role)-[:FbelongR]->(function)"));
    }
}
