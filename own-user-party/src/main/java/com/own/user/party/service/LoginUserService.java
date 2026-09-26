package com.own.user.party.service;

import com.own.face.party.LoginUserBean;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Role;
import com.own.user.party.dto.OwnProfileCommand;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

@Service
public class LoginUserService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoO5E0JQGsBkHzL8NnZ7q2kD4uYx2aRzQW";

    private final LoginUserDao loginUserDao;
    private final PasswordEncoder passwordEncoder;

    public LoginUserService(LoginUserDao loginUserDao, PasswordEncoder passwordEncoder) {
        this.loginUserDao = loginUserDao;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginUser authenticate(String loginName, String rawPassword) {
        String normalizedLoginName = normalize(loginName);
        LoginUser user = normalizedLoginName.isEmpty()
                ? null
                : loginUserDao.findByLoginName(normalizedLoginName);
        String storedPassword = user == null || user.getPassword() == null
                ? DUMMY_PASSWORD_HASH
                : user.getPassword();
        String passwordToCheck = rawPassword == null ? "" : rawPassword;
        boolean passwordMatches = passwordEncoder.matches(passwordToCheck, storedPassword);
        return user != null && passwordMatches ? user : null;
    }

    public boolean changePassword(String loginName, String currentPassword, String newPassword) {
        return changePasswordAndGet(loginName, currentPassword, newPassword) != null;
    }

    public LoginUser changePasswordAndGet(String loginName, String currentPassword, String newPassword) {
        requirePassword(newPassword);
        LoginUser user = authenticate(loginName, currentPassword);
        if (user == null) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return loginUserDao.save(user);
    }

    public LoginUser create(LoginUser user, String rawPassword) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        requireLoginName(user.getLoginName());
        requirePassword(rawPassword);
        user.setLoginName(normalize(user.getLoginName()));
        user.setPassword(passwordEncoder.encode(rawPassword));
        return loginUserDao.save(user);
    }

    public LoginUser updateProfile(Long id, LoginUserBean input) {
        if (id == null || input == null) {
            throw new IllegalArgumentException("user and id must not be null");
        }
        LoginUser user = getById(id);
        if (user == null) {
            throw TradeException.notFound("login user was not found");
        }
        if (input.getLoginUserName() != null) {
            requireLoginName(input.getLoginUserName());
            user.setLoginName(normalize(input.getLoginUserName()));
        }
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        if (input.getPartmentId() != null) {
            user.setPartmentId(input.getPartmentId());
        }
        user.setOrgId(input.getOrgId());
        return loginUserDao.save(user);
    }

    public LoginUser getById(Long id) {
        return id == null ? null : (LoginUser) loginUserDao.getLoginUser(id);
    }

    public LoginUser requireById(Long id) {
        LoginUser user = getById(id);
        if (user == null) throw TradeException.notFound("login user was not found");
        return user;
    }

    /** Binds the gateway-projected actor to the persisted account before self-service actions. */
    public LoginUser requireOwnedByActor(Long userId, TradeActor actor) {
        if (actor == null || (actor.getType() != ActorType.BUYER && actor.getType() != ActorType.MERCHANT)) {
            throw TradeException.forbidden("buyer or merchant actor is required");
        }
        return requireAuthorizationActor(userId, actor);
    }

    /** All actor types may read only their own persisted authorization projection. */
    public LoginUser requireAuthorizationActor(Long userId, TradeActor actor) {
        if (actor == null) throw TradeException.forbidden("authenticated actor is required");
        LoginUser user = requireById(userId);
        Long expectedActorId = user.getPartyId() == null ? user.getLoginUserId() : user.getPartyId();
        if (expectedActorId == null || !expectedActorId.equals(actor.getId())) {
            throw TradeException.forbidden("authenticated actor does not own this account");
        }
        return user;
    }

    /** Never accepts login name, party, role, organization or password changes. */
    public LoginUser updateOwnProfile(Long id, OwnProfileCommand input) {
        if (input == null) throw TradeException.unprocessable("profile payload is required");
        LoginUser user = requireById(id);
        boolean changed = false;
        if (input.getName() != null) { user.setName(requireText(input.getName(), "name", 100)); changed = true; }
        if (input.getEmail() != null) { user.setEmail(requireEmail(input.getEmail())); changed = true; }
        if (input.getPhone() != null) { user.setPhone(requirePhone(input.getPhone())); changed = true; }
        if (!changed) throw TradeException.unprocessable("at least one profile field is required");
        return loginUserDao.save(user);
    }

    public List<LoginUser> findAll() {
        List<LoginUser> users = loginUserDao.queryAllLoginUser();
        return users == null ? Collections.<LoginUser>emptyList() : users;
    }

    /**
     * Turns the existing role/function graph into standard claims. A self-registered
     * person with a party id is a buyer by default; merchant and system access must
     * be explicitly represented by ROLE_MERCHANT and ROLE_SYSTEM graph roles.
     */
    public LoginAuthorization authorization(LoginUser user, String requestedActorType) {
        if (user == null || user.getLoginUserId() == null) {
            throw TradeException.forbidden("authenticated user was not found");
        }
        List<Role> graphRoles = loginUserDao.findRolesByLoginUserId(user.getLoginUserId());
        List<String> roles = new ArrayList<String>();
        if (graphRoles != null) {
            for (Role role : graphRoles) if (role != null && role.getName() != null && !role.getName().trim().isEmpty()) roles.add(normalizeRole(role.getName()));
        }
        if (roles.isEmpty() && user.getPartyId() != null) roles.add("ROLE_BUYER");
        List<String> permissions = loginUserDao.findPermissionNamesByLoginUserId(user.getLoginUserId());
        if (permissions == null) permissions = Collections.emptyList();
        ActorType selected = selectActorType(roles, requestedActorType);
        Long actorId = user.getPartyId() == null ? user.getLoginUserId() : user.getPartyId();
        return new LoginAuthorization(actorId, selected, roles, permissions);
    }

    private void requireLoginName(String loginName) {
        String normalized = normalize(loginName);
        if (!normalized.matches("^[A-Za-z0-9._-]{3,64}$")) throw new IllegalArgumentException("login name must be 3-64 safe characters");
    }

    private String requireText(String value, String field, int maximum) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maximum) throw TradeException.unprocessable(field + " is invalid");
        return normalized;
    }

    private String requireEmail(String value) {
        String normalized = requireText(value, "email", 254);
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw TradeException.unprocessable("email is invalid");
        return normalized;
    }

    private String requirePhone(String value) {
        String normalized = requireText(value, "phone", 32);
        if (!normalized.matches("^[0-9+() -]{6,32}$")) throw TradeException.unprocessable("phone is invalid");
        return normalized;
    }

    private void requirePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128 || password.matches(".*[\\p{Cntrl}].*")) {
            throw new IllegalArgumentException("password must be 8-128 characters without control characters");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private ActorType selectActorType(List<String> roles, String requested) {
        ActorType desired;
        try {
            desired = requested == null || requested.trim().isEmpty()
                    ? defaultActorType(roles)
                    : ActorType.valueOf(requested.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw TradeException.unprocessable("actorType must be BUYER, MERCHANT or SYSTEM");
        }
        if (!roles.contains("ROLE_" + desired.name())) {
            throw TradeException.forbidden("requested actorType is not granted to this user");
        }
        return desired;
    }

    private ActorType defaultActorType(List<String> roles) {
        for (ActorType type : new ActorType[] {ActorType.BUYER, ActorType.MERCHANT, ActorType.SYSTEM}) {
            if (roles.contains("ROLE_" + type.name())) return type;
        }
        throw TradeException.forbidden("no marketplace role is granted to this user");
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
