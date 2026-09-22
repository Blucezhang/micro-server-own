package com.own.user.party.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.user.party.dao.domain.LoginUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginUserResponseTest {

    @Test
    public void serializedResponseDoesNotContainPasswordFields() throws Exception {
        LoginUser user = new LoginUser();
        user.setLoginUserId(7L);
        user.setLoginName("alice");
        user.setPassword("secret-password");
        user.setTrspwd("transaction-secret");

        String json = new ObjectMapper().writeValueAsString(LoginUserResponse.from(user));

        assertTrue(json.contains("\"loginName\":\"alice\""));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-password"));
        assertFalse(json.contains("trspwd"));
        assertFalse(json.contains("transaction-secret"));
    }

    @Test
    public void entitySerializationAlsoIgnoresSensitiveFields() throws Exception {
        LoginUser user = new LoginUser();
        user.setLoginName("alice");
        user.setPassword("secret-password");
        user.setTrspwd("transaction-secret");

        String json = new ObjectMapper().writeValueAsString(user);

        assertTrue(json.contains("\"loginName\":\"alice\""));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-password"));
        assertFalse(json.contains("trspwd"));
        assertFalse(json.contains("transaction-secret"));
    }
}
