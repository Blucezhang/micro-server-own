package com.own.user.party.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.service.LoginAuthorization;
import com.own.user.party.service.LoginUserService;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Returns the persisted authorization projection for the authenticated account only. */
@RestController
@RequestMapping("/api/v1/account/authorization")
public class AccountAuthorizationController extends BaseController {
    private final LoginUserService users;

    public AccountAuthorizationController(LoginUserService users) { this.users = users; }

    @GetMapping
    public Resp current(HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        LoginUser user = users.requireAuthorizationActor(userId(request), actor);
        LoginAuthorization authorization = users.authorization(user, actor.getType().name());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", user.getLoginUserId());
        result.put("actorId", authorization.getActorId());
        result.put("actorType", authorization.getActorType());
        result.put("roles", authorization.getRoles());
        result.put("permissions", authorization.getPermissions());
        return new Resp(result);
    }

    private Long userId(HttpServletRequest request) {
        String value = request == null ? null : request.getHeader("X-User-Id");
        if (value == null || !value.matches("[0-9]+")) throw TradeException.forbidden("authenticated user id is required");
        return Long.valueOf(value);
    }
}
