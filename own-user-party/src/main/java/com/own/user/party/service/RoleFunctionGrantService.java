package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.FunDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.Fun;
import com.own.user.party.dao.domain.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Maintains legacy role-function graph edges without accepting dangling IDs. */
@Service
public class RoleFunctionGrantService {
    private final RoleDao roles;
    private final FunDao functions;

    public RoleFunctionGrantService(RoleDao roles, FunDao functions) {
        this.roles = roles;
        this.functions = functions;
    }

    public List<Long> grant(Long roleId, List<Long> functionIds) {
        if (roleId == null || roleId.longValue() <= 0L) {
            throw TradeException.unprocessable("role id must be positive");
        }
        Role role = roles.findById(roleId).orElse(null);
        if (role == null) throw TradeException.notFound("role was not found");
        if (functionIds == null || functionIds.isEmpty()) {
            throw TradeException.unprocessable("at least one function id is required");
        }
        Set<Long> distinctIds = new LinkedHashSet<Long>();
        for (Long functionId : functionIds) {
            if (functionId == null || functionId.longValue() <= 0L) {
                throw TradeException.unprocessable("function id must be positive");
            }
            distinctIds.add(functionId);
        }
        List<Long> granted = new ArrayList<Long>();
        for (Long functionId : distinctIds) {
            Fun function = functions.findById(functionId).orElse(null);
            if (function == null) throw TradeException.notFound("function was not found");
            roles.createRelationShipRoleAndFun(roleId, functionId);
            granted.add(functionId);
        }
        return Collections.unmodifiableList(granted);
    }
}
