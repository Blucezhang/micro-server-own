package com.own.user.party.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.user.party.dto.LoginUserResponse;
import com.own.user.party.dto.OwnProfileCommand;
import com.own.user.party.service.LoginUserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Self-service account profile. Administrative graph endpoints remain SYSTEM-only. */
@RestController
@RequestMapping("/api/v1/account/profile")
public class AccountProfileController extends BaseController {
    private final LoginUserService users;

    public AccountProfileController(LoginUserService users) { this.users = users; }

    @GetMapping
    public Resp get(HttpServletRequest request) {
        return new Resp(LoginUserResponse.from(users.requireOwnedByActor(userId(request), TradeHeaders.actor(request))));
    }

    @PutMapping
    public Resp update(@RequestBody OwnProfileCommand command, HttpServletRequest request) {
        TradeHeaders.idempotencyKey(request);
        Long userId = userId(request);
        users.requireOwnedByActor(userId, TradeHeaders.actor(request));
        return new Resp(LoginUserResponse.from(users.updateOwnProfile(userId, command)));
    }

    private Long userId(HttpServletRequest request) {
        String value = request == null ? null : request.getHeader("X-User-Id");
        if (value == null || !value.matches("[0-9]+")) {
            throw com.own.face.trade.TradeException.forbidden("authenticated user id is required");
        }
        return Long.valueOf(value);
    }
}
