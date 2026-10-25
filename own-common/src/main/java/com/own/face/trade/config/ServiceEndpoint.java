package com.own.face.trade.config;

import java.net.URI;

/** Validate service roots at startup; request paths remain versioned code contracts. */
public final class ServiceEndpoint {
    private ServiceEndpoint() { }

    public static String requireRoot(String value) {
        URI uri;
        try { uri = URI.create(value); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("invalid service base URL", exception); }
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null
                || uri.getFragment() != null || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))
                || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("service base URL must be an HTTP(S) origin without credentials, path, query or fragment");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
