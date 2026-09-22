package com.own.face.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.ActorType;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class JwtTokenServiceTest {
    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    public void issuesAndVerifiesActorAndRbacClaims() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), SECRET, 120L, "test-issuer");
        String token = service.issue(10L, 20L, ActorType.MERCHANT,
                Arrays.asList("ROLE_MERCHANT"), Arrays.asList("product:write"));
        JwtPrincipal principal = service.verify(token);
        assertEquals(Long.valueOf(10L), principal.getUserId());
        assertEquals(Long.valueOf(20L), principal.getActorId());
        assertEquals(ActorType.MERCHANT, principal.getActorType());
        assertEquals("ROLE_MERCHANT", principal.getRoles().get(0));
        assertEquals("product:write", principal.getPermissions().get(0));
    }

    @Test
    public void rejectsTamperedToken() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), SECRET, 120L, "test-issuer");
        String token = service.issue(10L, 10L, ActorType.BUYER, null, null);
        int signatureStart = token.lastIndexOf('.') + 1;
        char firstSignatureCharacter = token.charAt(signatureStart);
        char replacement = firstSignatureCharacter == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1);
        try { service.verify(tampered); fail("tampered token must fail"); }
        catch (RuntimeException expected) { assertEquals(403, ((com.own.face.trade.TradeException) expected).getStatus()); }
    }

    @Test
    public void accessAndRefreshTokensCannotBeUsedInterchangeably() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), SECRET, 120L, "test-issuer");
        String access = service.issue(10L, 20L, ActorType.MERCHANT, null, null);
        String refresh = service.issueRefresh(10L, ActorType.MERCHANT);
        assertEquals(Long.valueOf(10L), service.verifyRefresh(refresh).getUserId());
        try { service.verify(refresh); fail("refresh token must not reach an API"); } catch (RuntimeException expected) { }
        try { service.verifyRefresh(access); fail("access token must not refresh a session"); } catch (RuntimeException expected) { }
    }

    @Test
    public void verifierAcceptsPreviousSecretOnlyDuringRotationWindow() {
        JwtTokenService old = new JwtTokenService(new ObjectMapper(), SECRET, 120L, "test-issuer");
        String oldToken = old.issue(10L, 10L, ActorType.BUYER, null, null);
        JwtTokenService rotated = new JwtTokenService(new ObjectMapper(), "abcdefabcdefabcdefabcdefabcdef12", SECRET, 120L, 120L, "test-issuer");
        assertEquals(Long.valueOf(10L), rotated.verify(oldToken).getUserId());
    }

    @Test
    public void refusesWeakSigningSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenService(new ObjectMapper(), "short", 120L, "test-issuer")
                .issue(1L, 1L, ActorType.BUYER, null, null));
    }
}
