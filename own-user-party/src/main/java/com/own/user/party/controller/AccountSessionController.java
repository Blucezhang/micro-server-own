package com.own.user.party.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.service.LoginUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Revokes every server-side refresh session belonging to the authenticated user. */
@RestController
@RequestMapping("/api/v1/account/sessions")
public class AccountSessionController {
    private final RefreshSessionService sessions;
    private final LoginUserService users;
    public AccountSessionController(RefreshSessionService sessions, LoginUserService users) { this.sessions = sessions; this.users = users; }

    @DeleteMapping
    public Resp revokeAll(HttpServletRequest request) {
        TradeHeaders.idempotencyKey(request);
        TradeActor actor = TradeHeaders.actor(request);
        Long userId = userId(request);
        users.requireOwnedByActor(userId, actor);
        sessions.revokeAllForUser(userId);
        return new Resp("all_sessions_revoked");
    }

    private Long userId(HttpServletRequest request) {
        String rawUserId = request == null ? null : request.getHeader("X-User-Id");
        if (rawUserId == null || !rawUserId.matches("[0-9]+")) throw TradeException.forbidden("authenticated user id is required");
        return Long.valueOf(rawUserId);
    }
}
