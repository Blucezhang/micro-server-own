package com.own.user.party.controller;

import com.own.face.party.LoginUserBean;
import com.own.face.party.UserBean;
import com.own.face.trade.TradeException;
import com.own.face.security.JwtTokenService;
import com.own.face.util.Resp;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dto.LoginUserResponse;
import com.own.user.party.dto.TokenLoginCommand;
import com.own.user.party.auth.dto.RefreshTokenCommand;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.auth.service.LoginAttemptService;
import com.own.user.party.service.LoginAuthorization;
import com.own.user.party.service.LoginUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户。
 */
@RestController
@RequestMapping("/login")
public class LoginUserController {

    private static final String AUTHENTICATION_FAILED = "authentication_failed";

    private final LoginUserService loginUserService;
    private final LoginUserDao loginUserDao;
    private final JwtTokenService jwtTokenService;
    private final RefreshSessionService refreshSessions;
    private final LoginAttemptService loginAttempts;

    public LoginUserController(LoginUserService loginUserService, LoginUserDao loginUserDao, JwtTokenService jwtTokenService, RefreshSessionService refreshSessions, LoginAttemptService loginAttempts) {
        this.loginUserService = loginUserService;
        this.loginUserDao = loginUserDao;
        this.jwtTokenService = jwtTokenService;
        this.refreshSessions = refreshSessions;
        this.loginAttempts = loginAttempts;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/Login")
    public Resp login(@RequestBody UserBean userBean) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (userBean != null) loginAttempts.requireAllowed(userBean.getLoginUserName());
        LoginUser user = userBean == null
                ? null
                : loginUserService.authenticate(userBean.getLoginUserName(), userBean.getPassword());
        if (user == null) {
            if (userBean != null) loginAttempts.failed(userBean.getLoginUserName());
            result.put("message", AUTHENTICATION_FAILED);
            return new Resp(result);
        }
        loginAttempts.succeeded(userBean.getLoginUserName());
        result.put("LoginUser", LoginUserResponse.from(user));
        return new Resp(result);
    }

    @Operation(summary = "获取 JWT 访问令牌")
    @PostMapping("/token")
    public Resp token(@RequestBody TokenLoginCommand command) {
        if (command != null) loginAttempts.requireAllowed(command.getLoginUserName());
        LoginUser user = command == null ? null : loginUserService.authenticate(command.getLoginUserName(), command.getPassword());
        if (user == null) { if (command != null) loginAttempts.failed(command.getLoginUserName()); throw TradeException.forbidden(AUTHENTICATION_FAILED); }
        loginAttempts.succeeded(command.getLoginUserName());
        LoginAuthorization authorization = loginUserService.authorization(user, command.getActorType());
        return new Resp(refreshSessions.issue(user, authorization));
    }

    @PostMapping("/refresh")
    public Resp refresh(@RequestBody RefreshTokenCommand command) {
        if (command == null || command.getRefreshToken() == null || command.getRefreshToken().trim().isEmpty()) throw TradeException.badRequest("refreshToken is required");
        return new Resp(refreshSessions.rotate(command.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Resp logout(@RequestBody RefreshTokenCommand command) {
        if (command == null || command.getRefreshToken() == null || command.getRefreshToken().trim().isEmpty()) throw TradeException.badRequest("refreshToken is required");
        refreshSessions.revoke(command.getRefreshToken()); return new Resp("logged_out");
    }

    @GetMapping("/sessions")
    public Resp activeSessions(@RequestHeader("X-User-Id") Long userId, HttpServletRequest request) {
        loginUserService.requireOwnedByActor(userId, com.own.face.trade.TradeHeaders.actor(request));
        return new Resp(refreshSessions.listActive(userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Resp revokeSession(@RequestHeader("X-User-Id") Long userId, @PathVariable Long sessionId, HttpServletRequest request) {
        com.own.face.trade.TradeHeaders.idempotencyKey(request);
        loginUserService.requireOwnedByActor(userId, com.own.face.trade.TradeHeaders.actor(request));
        refreshSessions.revokeOwnedSession(userId, sessionId);
        return new Resp("session_revoked");
    }

    @Operation(summary = "根据用户Id查询登录用户的Detail")
    @GetMapping("/getLoginUser/{loginId}")
    public Resp getDetail(@PathVariable Long loginId) {
        return new Resp(LoginUserResponse.from(loginUserService.getById(loginId)));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/updatePassword")
    public Resp updatePassword(@RequestBody UserBean userBean) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (userBean == null) {
            result.put("message", AUTHENTICATION_FAILED);
            return new Resp(result);
        }
        try {
            loginAttempts.requireAllowed(userBean.getLoginUserName());
            LoginUser changed = loginUserService.changePasswordAndGet(
                    userBean.getLoginUserName(),
                    userBean.getPassword(),
                    userBean.getNewpassword());
            if (changed != null) {
                loginAttempts.succeeded(userBean.getLoginUserName());
                refreshSessions.revokeAllForUser(changed.getLoginUserId());
            } else {
                loginAttempts.failed(userBean.getLoginUserName());
            }
            result.put("message", changed != null ? "password_updated" : AUTHENTICATION_FAILED);
        } catch (IllegalArgumentException exception) {
            result.put("message", "invalid_request");
        }
        return new Resp(result);
    }

    @Operation(summary = "查询所有LoginUser用户")
    @GetMapping("/LoginUser")
    @ResponseBody
    public Resp queryAllLoginUser() {
        List<LoginUserResponse> users = loginUserService.findAll().stream()
                .map(LoginUserResponse::from)
                .collect(Collectors.toList());
        return new Resp(users);
    }

    @Operation(summary = "创建LoginUser")
    @PutMapping("/LoginUser")
    @ResponseBody
    public void createLoginUser(@RequestBody LoginUserBean userBean) {
        LoginUser user = new LoginUser();
        user.setName(userBean.getName());
        user.setEmail(userBean.getEmail());
        user.setLoginName(userBean.getLoginUserName());
        user.setPhone(userBean.getPhone());
        user = loginUserService.create(user, userBean.getPassword());

        loginUserDao.createRelationShipWithLoginUser(user.getLoginUserId());
        if (userBean.getPartmentId() != null) {
            loginUserDao.createRelationShipLoginUserAndOrg(
                    user.getLoginUserId(), userBean.getPartmentId());
        } else {
            loginUserDao.createRelationShipLoginUserAndOrg(
                    user.getLoginUserId(), userBean.getOrgId());
        }
    }

    @Operation(summary = "根据Id修改LoginUser")
    @PostMapping("/LoginUser/{id}")
    public void updateLoginUser(@RequestBody LoginUserBean loginUserBean,
                                @PathVariable Long id) {
        LoginUser user = loginUserService.updateProfile(id, loginUserBean);
        if (user == null) {
            throw TradeException.notFound("login user was not found");
        }
        loginUserDao.deleteLoginUserAndOrgs(id);
        if (loginUserBean.getPartmentId() != null) {
            loginUserDao.createRelationShipLoginUserAndOrg(
                    user.getLoginUserId(), loginUserBean.getPartmentId());
        } else {
            loginUserDao.createRelationShipLoginUserAndOrg(
                    user.getLoginUserId(), loginUserBean.getOrgId());
        }
    }

    @Operation(summary = "根据ID删除系统用户信息")
    @DeleteMapping("/LoginUser/{id}")
    public void deleteLoginUser(@PathVariable Integer id) {
        loginUserDao.deleteLoginUser(id);
    }
}
