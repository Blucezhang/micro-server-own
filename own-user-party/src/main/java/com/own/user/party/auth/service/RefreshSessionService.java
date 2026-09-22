package com.own.user.party.auth.service;

import com.own.face.security.JwtRefreshPrincipal;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.TradeException;
import com.own.user.party.auth.domain.RefreshTokenSession;
import com.own.user.party.auth.repository.RefreshTokenSessionRepository;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.service.LoginAuthorization;
import com.own.user.party.service.LoginUserService;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshSessionService {
    private final JwtTokenService tokens; private final RefreshTokenSessionRepository sessions; private final LoginUserService users;
    public RefreshSessionService(JwtTokenService tokens, RefreshTokenSessionRepository sessions, LoginUserService users) { this.tokens = tokens; this.sessions = sessions; this.users = users; }
    @Transactional public Map<String, Object> issue(LoginUser user, LoginAuthorization authorization) {
        String refresh = tokens.issueRefresh(user.getLoginUserId(), authorization.getActorType()); JwtRefreshPrincipal principal = tokens.verifyRefresh(refresh);
        sessions.save(new RefreshTokenSession(principal.getTokenId(), principal.getUserId(), principal.getActorType().name(), principal.getExpiresAt()));
        return response(tokens.issue(user.getLoginUserId(), authorization.getActorId(), authorization.getActorType(), authorization.getRoles(), authorization.getPermissions()), refresh, authorization);
    }
    @Transactional public Map<String, Object> rotate(String rawRefreshToken) {
        JwtRefreshPrincipal principal = tokens.verifyRefresh(rawRefreshToken); RefreshTokenSession current = sessions.findByTokenIdForUpdate(principal.getTokenId());
        if (current == null || !current.getUserId().equals(principal.getUserId()) || !current.getActorType().equals(principal.getActorType().name()) || !current.isActiveAt(new Date())) throw TradeException.forbidden("refresh session is expired or revoked");
        LoginUser user = users.getById(principal.getUserId()); if (user == null) throw TradeException.forbidden("authenticated user was not found");
        LoginAuthorization authorization = users.authorization(user, principal.getActorType().name()); String refresh = tokens.issueRefresh(user.getLoginUserId(), authorization.getActorType()); JwtRefreshPrincipal next = tokens.verifyRefresh(refresh);
        current.replace(next.getTokenId()); sessions.save(current); sessions.save(new RefreshTokenSession(next.getTokenId(), next.getUserId(), next.getActorType().name(), next.getExpiresAt()));
        return response(tokens.issue(user.getLoginUserId(), authorization.getActorId(), authorization.getActorType(), authorization.getRoles(), authorization.getPermissions()), refresh, authorization);
    }
    @Transactional public void revoke(String rawRefreshToken) { JwtRefreshPrincipal principal = tokens.verifyRefresh(rawRefreshToken); RefreshTokenSession current = sessions.findByTokenIdForUpdate(principal.getTokenId()); if (current != null && current.getUserId().equals(principal.getUserId())) { current.revoke(); sessions.save(current); } }
    @Transactional(readOnly = true) public List<Map<String, Object>> listActive(Long userId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (RefreshTokenSession session : sessions.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE")) {
            if (!session.isActiveAt(new Date())) continue;
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("id", session.getId()); item.put("actorType", session.getActorType()); item.put("createdAt", session.getCreatedAt()); item.put("expiresAt", session.getExpiresAt());
            result.add(item);
        }
        return result;
    }
    @Transactional public void revokeOwnedSession(Long userId, Long sessionId) {
        RefreshTokenSession session = sessions.findByIdForUpdate(sessionId);
        if (session == null) throw TradeException.notFound("refresh session was not found");
        if (!userId.equals(session.getUserId())) throw TradeException.forbidden("refresh session does not belong to authenticated user");
        session.revoke(); sessions.save(session);
    }
    @Transactional public void revokeAllForUser(Long userId) {
        if (userId != null) sessions.revokeActiveByUserId(userId, new Date());
    }
    private Map<String, Object> response(String access, String refresh, LoginAuthorization authorization) { Map<String, Object> result = new LinkedHashMap<String, Object>(); result.put("accessToken", access); result.put("tokenType", "Bearer"); result.put("expiresAt", tokens.expiresAt()); result.put("refreshToken", refresh); result.put("refreshExpiresAt", tokens.refreshExpiresAt()); result.put("actorId", authorization.getActorId()); result.put("actorType", authorization.getActorType()); result.put("roles", authorization.getRoles()); result.put("permissions", authorization.getPermissions()); return result; }
}
