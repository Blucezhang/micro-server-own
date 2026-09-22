package com.own.user.party.dto;

/** Login payload for a marketplace JWT. actorType selects an already granted role. */
public class TokenLoginCommand {
    private String loginUserName;
    private String password;
    private String actorType;

    public String getLoginUserName() { return loginUserName; }
    public void setLoginUserName(String loginUserName) { this.loginUserName = loginUserName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
}
