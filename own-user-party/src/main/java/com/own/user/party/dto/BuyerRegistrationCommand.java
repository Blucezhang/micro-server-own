package com.own.user.party.dto;

/** Anonymous registration is intentionally limited to a buyer account. */
public class BuyerRegistrationCommand {
    private String loginName;
    private String password;
    private String name;
    private String email;
    private String phone;

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
