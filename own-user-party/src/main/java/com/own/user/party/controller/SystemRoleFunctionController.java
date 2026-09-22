package com.own.user.party.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.user.party.dto.RoleFunctionGrantCommand;
import com.own.user.party.service.RoleFunctionGrantService;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Versioned SYSTEM-only route for appending legacy graph permissions to a role. */
@RestController
@RequestMapping("/api/v1/system/roles")
public class SystemRoleFunctionController {
    private final RoleFunctionGrantService grants;

    public SystemRoleFunctionController(RoleFunctionGrantService grants) { this.grants = grants; }

    @PutMapping("/{roleId}/functions")
    public Resp grant(@PathVariable Long roleId, @RequestBody RoleFunctionGrantCommand command,
                      HttpServletRequest request) {
        TradeHeaders.actor(request).require(ActorType.SYSTEM);
        TradeHeaders.idempotencyKey(request);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("roleId", roleId);
        result.put("functionIds", grants.grant(roleId, command == null ? null : command.getFunctionIds()));
        return new Resp(result, 200, "role_functions_granted");
    }
}
