package com.own.user.party.service;

import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Role;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MerchantRoleGrantServiceTest {
    @Test
    public void existingMerchantRoleIsGrantedOnceAndAllRefreshSessionsAreRevoked() {
        LoginUserService users = mock(LoginUserService.class);
        RoleDao roles = mock(RoleDao.class);
        LoginUserDao loginUsers = mock(LoginUserDao.class);
        RefreshSessionService sessions = mock(RefreshSessionService.class);
        LoginUser user = user(7L, 88L);
        Role merchant = role(12L, "merchant");
        when(users.requireById(7L)).thenReturn(user);
        when(roles.findMarketplaceMerchantRole()).thenReturn(merchant);

        Map<String, Object> result = new MerchantRoleGrantService(users, roles, loginUsers, sessions).grantMerchantRole(7L);

        verify(loginUsers).grantRoleToLoginUserIfAbsent(7L, 12L);
        verify(sessions).revokeAllForUser(7L);
        assertEquals("ROLE_MERCHANT", result.get("role"));
        assertEquals(Boolean.TRUE, result.get("refreshSessionsRevoked"));
    }

    @Test
    public void canonicalRoleIsCreatedWhenLegacyGraphHasNone() {
        LoginUserService users = mock(LoginUserService.class);
        RoleDao roles = mock(RoleDao.class);
        LoginUserDao loginUsers = mock(LoginUserDao.class);
        RefreshSessionService sessions = mock(RefreshSessionService.class);
        when(users.requireById(7L)).thenReturn(user(7L, null));
        Role saved = role(13L, "ROLE_MERCHANT");
        when(roles.save(any(Role.class))).thenReturn(saved);

        new MerchantRoleGrantService(users, roles, loginUsers, sessions).grantMerchantRole(7L);

        verify(roles).save(any(Role.class));
        verify(loginUsers).grantRoleToLoginUserIfAbsent(7L, 13L);
        verify(sessions).revokeAllForUser(7L);
    }

    private LoginUser user(Long id, Long partyId) {
        LoginUser user = new LoginUser(); user.setLoginUserId(id); user.setPartyId(partyId); return user;
    }

    private Role role(Long id, String name) {
        Role role = new Role(); role.setRole(id); role.setName(name); return role;
    }
}
