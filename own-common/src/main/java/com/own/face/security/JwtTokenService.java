package com.own.face.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Minimal HS256 JWT issuer/verifier kept dependency-free for the Java 8 stack. */
@Component
public class JwtTokenService {
    private static final String HMAC = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final String previousSecret;
    private final long expirationSeconds;
    private final long refreshExpirationSeconds;
    private final String issuer;

    /** Compatibility constructor for focused tests and existing callers. */
    public JwtTokenService(ObjectMapper objectMapper, String secret, long expirationSeconds, String issuer) {
        this(objectMapper, secret, "", expirationSeconds, 1209600L, issuer);
    }

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${trade.security.jwt.secret:}") String secret,
                           @Value("${trade.security.jwt.previous-secret:}") String previousSecret,
                           @Value("${trade.security.jwt.expiration-seconds:7200}") long expirationSeconds,
                           @Value("${trade.security.jwt.refresh-expiration-seconds:1209600}") long refreshExpirationSeconds,
                           @Value("${trade.security.jwt.issuer:micro-server-own}") String issuer) {
        this.objectMapper = objectMapper;
        this.secret = secret == null ? "" : secret;
        this.previousSecret = previousSecret == null ? "" : previousSecret;
        this.expirationSeconds = expirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.issuer = issuer;
    }

    public String issue(Long userId, Long actorId, ActorType actorType,
                        List<String> roles, List<String> permissions) {
        if (userId == null || userId.longValue() <= 0 || actorId == null || actorId.longValue() <= 0 || actorType == null) {
            throw TradeException.unprocessable("authenticated user identity is incomplete");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("trade.security.jwt.secret must contain at least 32 bytes");
        }
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new LinkedHashMap<String, Object>();
        header.put("alg", "HS256"); header.put("typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<String, Object>();
        claims.put("iss", issuer); claims.put("sub", String.valueOf(userId));
        claims.put("actorId", actorId); claims.put("actorType", actorType.name());
        claims.put("tokenUse", "access"); claims.put("roles", roles == null ? new ArrayList<String>() : roles);
        claims.put("permissions", permissions == null ? new ArrayList<String>() : permissions);
        claims.put("iat", now); claims.put("exp", now + expirationSeconds); claims.put("jti", UUID.randomUUID().toString());
        try {
            String unsigned = encode(objectMapper.writeValueAsBytes(header)) + "." + encode(objectMapper.writeValueAsBytes(claims));
            return unsigned + "." + encode(sign(unsigned));
        } catch (Exception exception) {
            throw new IllegalStateException("could not create JWT", exception);
        }
    }

    public String issueRefresh(Long userId, ActorType actorType) {
        if (userId == null || userId.longValue() <= 0 || actorType == null) throw TradeException.unprocessable("authenticated user identity is incomplete");
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalStateException("trade.security.jwt.secret must contain at least 32 bytes");
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new LinkedHashMap<String, Object>(); header.put("alg", "HS256"); header.put("typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<String, Object>();
        claims.put("iss", issuer); claims.put("sub", String.valueOf(userId)); claims.put("actorType", actorType.name());
        claims.put("tokenUse", "refresh"); claims.put("iat", now); claims.put("exp", now + refreshExpirationSeconds); claims.put("jti", UUID.randomUUID().toString());
        try { String unsigned = encode(objectMapper.writeValueAsBytes(header)) + "." + encode(objectMapper.writeValueAsBytes(claims)); return unsigned + "." + encode(sign(unsigned)); }
        catch (Exception exception) { throw new IllegalStateException("could not create refresh JWT", exception); }
    }

    public JwtPrincipal verify(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.", -1);
            if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) throw invalid();
            Map<String, Object> header = read(parts[0]);
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) throw invalid();
            if (!signatureMatches(parts[0] + "." + parts[1], URL_DECODER.decode(parts[2]))) throw invalid();
            Map<String, Object> claims = read(parts[1]);
            if (!issuer.equals(String.valueOf(claims.get("iss"))) || !"access".equals(String.valueOf(claims.get("tokenUse"))) || number(claims.get("exp")) <= Instant.now().getEpochSecond()) throw invalid();
            Long userId = Long.valueOf(String.valueOf(claims.get("sub")));
            Long actorId = Long.valueOf(number(claims.get("actorId")));
            if (userId.longValue() <= 0 || actorId.longValue() <= 0) throw invalid();
            ActorType actorType = ActorType.valueOf(String.valueOf(claims.get("actorType")));
            return new JwtPrincipal(userId, actorId, actorType, strings(claims.get("roles")), strings(claims.get("permissions")));
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    public JwtRefreshPrincipal verifyRefresh(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.", -1);
            if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) throw invalid();
            Map<String, Object> header = read(parts[0]); if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) throw invalid();
            if (!signatureMatches(parts[0] + "." + parts[1], URL_DECODER.decode(parts[2]))) throw invalid();
            Map<String, Object> claims = read(parts[1]); long expires = number(claims.get("exp"));
            if (!issuer.equals(String.valueOf(claims.get("iss"))) || !"refresh".equals(String.valueOf(claims.get("tokenUse"))) || expires <= Instant.now().getEpochSecond()) throw invalid();
            Long userId = Long.valueOf(String.valueOf(claims.get("sub"))); String tokenId = String.valueOf(claims.get("jti"));
            if (userId.longValue() <= 0 || tokenId.trim().isEmpty()) throw invalid();
            return new JwtRefreshPrincipal(userId, ActorType.valueOf(String.valueOf(claims.get("actorType"))), tokenId, new Date(expires * 1000L));
        } catch (TradeException exception) { throw exception; } catch (Exception exception) { throw invalid(); }
    }

    public Date expiresAt() { return new Date(System.currentTimeMillis() + expirationSeconds * 1000L); }
    public Date refreshExpiresAt() { return new Date(System.currentTimeMillis() + refreshExpirationSeconds * 1000L); }

    private Map<String, Object> read(String value) throws Exception {
        return objectMapper.readValue(URL_DECODER.decode(value), new TypeReference<Map<String, Object>>() { });
    }
    private String encode(byte[] bytes) { return URL_ENCODER.encodeToString(bytes); }
    private boolean signatureMatches(String unsigned, byte[] actual) throws Exception {
        if (MessageDigest.isEqual(sign(unsigned, secret), actual)) return true;
        return !previousSecret.trim().isEmpty() && MessageDigest.isEqual(sign(unsigned, previousSecret), actual);
    }
    private byte[] sign(String value) throws Exception { return sign(value, secret); }
    private byte[] sign(String value, String signingSecret) throws Exception { Mac mac = Mac.getInstance(HMAC); mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), HMAC)); return mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); }
    private long number(Object value) { return Long.parseLong(String.valueOf(value)); }
    private List<String> strings(Object source) { List<String> result = new ArrayList<String>(); if (source instanceof Iterable) for (Object item : (Iterable<?>) source) result.add(String.valueOf(item)); return result; }
    private TradeException invalid() { return TradeException.forbidden("invalid or expired bearer token"); }
}
