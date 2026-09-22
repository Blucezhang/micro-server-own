package com.own.user.party.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.user.party.service.MerchantRoleGrantService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** System-only, audited-through-idempotency merchant-role administration. */
@RestController
@RequestMapping("/api/v1/system/accounts")
public class SystemMerchantAccountController {
    private final MerchantRoleGrantService merchantRoles;

    public SystemMerchantAccountController(MerchantRoleGrantService merchantRoles) {
        this.merchantRoles = merchantRoles;
    }

    @PutMapping("/{loginUserId}/merchant-role")
    public Resp grantMerchantRole(@PathVariable Long loginUserId, HttpServletRequest request) {
        TradeHeaders.actor(request).require(ActorType.SYSTEM);
        TradeHeaders.idempotencyKey(request);
        return new Resp(merchantRoles.grantMerchantRole(loginUserId), 200, "merchant_role_granted");
    }
}
