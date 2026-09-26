package com.own.user.party.service;

import com.own.face.party.LoginUserBean;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeActor;
import com.own.face.trade.ActorType;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Role;
import com.own.user.party.dto.OwnProfileCommand;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginUserServiceTest {

    private LoginUserDao loginUserDao;
    private PasswordEncoder passwordEncoder;
    private LoginUserService loginUserService;

    @BeforeEach
    public void setUp() {
        loginUserDao = mock(LoginUserDao.class);
        passwordEncoder = new BCryptPasswordEncoder(4);
        loginUserService = new LoginUserService(loginUserDao, passwordEncoder);
    }

    @Test
    public void authenticatesWithBCryptHash() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("correct-password"));
        when(loginUserDao.findByLoginName("alice")).thenReturn(storedUser);

        LoginUser authenticated = loginUserService.authenticate(" alice ", "correct-password");

        assertNotNull(authenticated);
        assertSame(storedUser, authenticated);
    }

    @Test
    public void rejectsUnknownUserAndWrongPasswordWithTheSameResult() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("correct-password"));
        when(loginUserDao.findByLoginName("alice")).thenReturn(storedUser);
        when(loginUserDao.findByLoginName("missing")).thenReturn(null);

        assertNull(loginUserService.authenticate("alice", "wrong-password"));
        assertNull(loginUserService.authenticate("missing", "wrong-password"));
    }

    @Test
    public void hashesPasswordBeforeCreatingUser() {
        LoginUser newUser = user("alice", null);
        when(loginUserDao.save(any(LoginUser.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        LoginUser savedUser = loginUserService.create(newUser, "plain-password");

        ArgumentCaptor<LoginUser> captor = ArgumentCaptor.forClass(LoginUser.class);
        verify(loginUserDao).save(captor.capture());
        assertNotEquals("plain-password", captor.getValue().getPassword());
        assertTrue(passwordEncoder.matches("plain-password", captor.getValue().getPassword()));
        assertSame(captor.getValue(), savedUser);
    }

    @Test
    public void rejectsWeakOrUnsafeCredentialsWhenCreatingUsers() {
        try { loginUserService.create(user("ab", null), "password-123"); fail("short login name must be rejected"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("login name")); }
        try { loginUserService.create(user("alice", null), "short"); fail("short password must be rejected"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("password")); }
        try { loginUserService.create(user("alice", null), "valid-pass\n"); fail("control character must be rejected"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("password")); }
    }

    @Test
    public void verifiesCurrentPasswordAndHashesReplacement() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("old-password"));
        when(loginUserDao.findByLoginName("alice")).thenReturn(storedUser);
        when(loginUserDao.save(storedUser)).thenReturn(storedUser);

        assertTrue(loginUserService.changePassword("alice", "old-password", "new-password"));

        assertFalse("new-password".equals(storedUser.getPassword()));
        assertTrue(passwordEncoder.matches("new-password", storedUser.getPassword()));
        verify(loginUserDao).save(storedUser);
    }

    @Test
    public void doesNotWriteWhenCurrentPasswordIsInvalid() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("old-password"));
        when(loginUserDao.findByLoginName("alice")).thenReturn(storedUser);

        assertFalse(loginUserService.changePassword("alice", "wrong-password", "new-password"));

        verify(loginUserDao, never()).save(any(LoginUser.class));
    }

    @Test
    public void profileUpdatePreservesPasswordHash() {
        String passwordHash = passwordEncoder.encode("current-password");
        LoginUser storedUser = user("alice", passwordHash);
        when(loginUserDao.getLoginUser(7L)).thenReturn(storedUser);
        when(loginUserDao.save(storedUser)).thenReturn(storedUser);
        LoginUserBean input = new LoginUserBean();
        input.setName("Alice Updated");
        input.setPassword("attempted-direct-reset");

        LoginUser updated = loginUserService.updateProfile(7L, input);

        assertNotNull(updated);
        assertEquals(passwordHash, updated.getPassword());
        assertEquals("Alice Updated", updated.getName());
    }

    @Test
    public void selfProfileUpdateCannotChangeAuthenticationOrAuthorizationFields() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("current-password"));
        storedUser.setLoginUserId(7L); storedUser.setPartyId(99L); storedUser.setOrgId(33L);
        when(loginUserDao.getLoginUser(7L)).thenReturn(storedUser);
        when(loginUserDao.save(storedUser)).thenReturn(storedUser);
        OwnProfileCommand input = new OwnProfileCommand();
        input.setName(" Alice Updated "); input.setEmail("alice@example.test"); input.setPhone("13800138000");

        LoginUser updated = loginUserService.updateOwnProfile(7L, input);

        assertEquals("alice", updated.getLoginName());
        assertEquals(Long.valueOf(99L), updated.getPartyId());
        assertEquals(Long.valueOf(33L), updated.getOrgId());
        assertTrue(passwordEncoder.matches("current-password", updated.getPassword()));
        assertEquals("Alice Updated", updated.getName());
    }

    @Test
    public void selfProfileRejectsInvalidEmail() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("current-password"));
        when(loginUserDao.getLoginUser(7L)).thenReturn(storedUser);
        OwnProfileCommand input = new OwnProfileCommand(); input.setEmail("not-an-email");
        assertThrows(TradeException.class, () -> loginUserService.updateOwnProfile(7L, input));
    }

    @Test
    public void selfServiceAccountMustMatchPersistedPartyActor() {
        LoginUser storedUser = user("alice", passwordEncoder.encode("current-password"));
        storedUser.setLoginUserId(7L); storedUser.setPartyId(99L);
        when(loginUserDao.getLoginUser(7L)).thenReturn(storedUser);
        assertSame(storedUser, loginUserService.requireOwnedByActor(7L, new TradeActor(99L, ActorType.BUYER)));
        try {
            loginUserService.requireOwnedByActor(7L, new TradeActor(100L, ActorType.BUYER));
            fail("different party actor must be rejected");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    @Test
    public void systemAuthorizationMustMatchItsPersistedActor() {
        LoginUser storedUser = user("admin", passwordEncoder.encode("current-password"));
        storedUser.setLoginUserId(7L);
        when(loginUserDao.getLoginUser(7L)).thenReturn(storedUser);
        assertSame(storedUser, loginUserService.requireAuthorizationActor(7L, new TradeActor(7L, ActorType.SYSTEM)));
        assertThrows(TradeException.class,
                () -> loginUserService.requireAuthorizationActor(7L, new TradeActor(8L, ActorType.SYSTEM)));
        assertThrows(TradeException.class,
                () -> loginUserService.requireOwnedByActor(7L, new TradeActor(7L, ActorType.SYSTEM)));
    }

    @Test
    public void missingProfileUpdateReturnsNotFound() {
        when(loginUserDao.getLoginUser(99L)).thenReturn(null);
        assertThrows(TradeException.class, () -> loginUserService.updateProfile(99L, new LoginUserBean()));
    }

    @Test
    public void partyUserGetsBuyerRoleByDefaultButNotMerchantRole() {
        LoginUser user = user("buyer", passwordEncoder.encode("password"));
        user.setLoginUserId(10L); user.setPartyId(99L);
        LoginAuthorization authorization = loginUserService.authorization(user, "BUYER");
        assertEquals(Long.valueOf(99L), authorization.getActorId());
        assertEquals("ROLE_BUYER", authorization.getRoles().get(0));
        try { loginUserService.authorization(user, "MERCHANT"); fail("buyer must not select merchant actor"); }
        catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    @Test
    public void graphMerchantRoleAllowsMerchantToken() {
        LoginUser user = user("merchant", passwordEncoder.encode("password"));
        user.setLoginUserId(11L); user.setPartyId(88L);
        Role merchant = new Role(); merchant.setName("merchant");
        when(loginUserDao.findRolesByLoginUserId(11L)).thenReturn(Arrays.asList(merchant));
        when(loginUserDao.findPermissionNamesByLoginUserId(11L)).thenReturn(Arrays.asList("product:write"));
        LoginAuthorization authorization = loginUserService.authorization(user, "MERCHANT");
        assertEquals("ROLE_MERCHANT", authorization.getRoles().get(0));
        assertEquals("product:write", authorization.getPermissions().get(0));
    }

    private LoginUser user(String loginName, String password) {
        LoginUser user = new LoginUser();
        user.setLoginName(loginName);
        user.setPassword(password);
        return user;
    }
}
