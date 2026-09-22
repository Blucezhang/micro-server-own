package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Role;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Controlled system administration for marketplace merchant access.
 *
 * <p>The role graph is Neo4j while refresh sessions are held in MySQL, so a
 * distributed transaction is not available in this legacy stack.  The role is
 * made durable first; refresh-session revocation then ensures that all later
 * tokens are issued from the new authorization projection.</p>
 */
@Service
public class MerchantRoleGrantService {
    private static final String MERCHANT_ROLE = "ROLE_MERCHANT";

    private final LoginUserService users;
    private final RoleDao roles;
    private final LoginUserDao loginUsers;
    private final RefreshSessionService refreshSessions;

    public MerchantRoleGrantService(LoginUserService users, RoleDao roles,
                                    LoginUserDao loginUsers, RefreshSessionService refreshSessions) {
        this.users = users;
        this.roles = roles;
        this.loginUsers = loginUsers;
        this.refreshSessions = refreshSessions;
    }

    public Map<String, Object> grantMerchantRole(Long loginUserId) {
        if (loginUserId == null || loginUserId.longValue() <= 0L) {
            throw TradeException.unprocessable("login user id must be positive");
        }
        LoginUser user = users.requireById(loginUserId);
        Role merchantRole = roles.findMarketplaceMerchantRole();
        if (merchantRole == null) {
            merchantRole = new Role();
            merchantRole.setName(MERCHANT_ROLE);
            merchantRole.setNote("Marketplace merchant role");
            merchantRole = roles.save(merchantRole);
        }
        if (merchantRole.getRole() == null) {
            throw TradeException.unprocessable("merchant role could not be persisted");
        }
        loginUsers.grantRoleToLoginUserIfAbsent(user.getLoginUserId(), merchantRole.getRole());
        refreshSessions.revokeAllForUser(user.getLoginUserId());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", user.getLoginUserId());
        result.put("actorId", user.getPartyId() == null ? user.getLoginUserId() : user.getPartyId());
        result.put("role", MERCHANT_ROLE);
        result.put("refreshSessionsRevoked", Boolean.TRUE);
        return result;
    }
}
