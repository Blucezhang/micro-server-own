package com.own.user.party.dto;

import com.own.user.party.dao.domain.LoginUser;

public final class LoginUserResponse {

    private final Long loginUserId;
    private final String loginName;
    private final Integer loginId;
    private final Long partyId;
    private final String name;
    private final String email;
    private final String phone;
    private final Long partmentId;
    private final Long orgId;

    private LoginUserResponse(LoginUser user) {
        this.loginUserId = user.getLoginUserId();
        this.loginName = user.getLoginName();
        this.loginId = user.getLoginId();
        this.partyId = user.getPartyId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.partmentId = user.getPartmentId();
        this.orgId = user.getOrgId();
    }

    public static LoginUserResponse from(LoginUser user) {
        return user == null ? null : new LoginUserResponse(user);
    }

    public Long getLoginUserId() {
        return loginUserId;
    }

    public String getLoginName() {
        return loginName;
    }

    public Integer getLoginId() {
        return loginId;
    }

    public Long getPartyId() {
        return partyId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getPartmentId() {
        return partmentId;
    }

    public Long getOrgId() {
        return orgId;
    }
}
