package com.own.product.api.gateway.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

/** Compatibility-named test class for the WebFlux replacement of the former Zuul filter. */
public class JwtAuthenticationFilterTest {

    @Test
    public void onlyVersionedStorefrontProductBrowseAndDetailAreAnonymous() {
        assertTrue(publicRequest(HttpMethod.GET, "/product/api/v1/products"));
        assertTrue(publicRequest(HttpMethod.GET, "/product/api/v1/products/7"));
        assertTrue(publicRequest(HttpMethod.GET, "/product/api/v1/categories"));
        assertFalse(publicRequest(HttpMethod.GET, "/product?partyId=1"));
    }

    @Test
    public void onlyExactPublicReviewCollectionIsAnonymous() {
        assertTrue(publicRequest(HttpMethod.GET, "/product/api/v1/products/7/reviews"));
        assertFalse(publicRequest(HttpMethod.GET, "/product/api/v1/products/7/reviews/9"));
        assertFalse(publicRequest(HttpMethod.GET, "/product/api/v1/products/7/internal-data"));
    }

    @Test
    public void publicAuthenticationRoutesMustMatchTheirExactGatewayPath() {
        assertFalse(publicRequest(HttpMethod.POST, "/other/user/login/token"));
        assertFalse(publicRequest(HttpMethod.POST, "/user/login/token/extra"));
        assertTrue(publicRequest(HttpMethod.POST, "/user/login/token"));
        assertTrue(publicRequest(HttpMethod.POST, "/user/login/refresh"));
        assertTrue(publicRequest(HttpMethod.POST, "/user/login/logout"));
    }

    @Test
    public void buyerRegistrationIsTheOnlyAnonymousMarketplaceWrite() {
        assertTrue(publicRequest(HttpMethod.POST, "/user/api/v1/auth/registrations"));
        assertFalse(publicRequest(HttpMethod.GET, "/user/api/v1/auth/registrations"));
        assertFalse(publicRequest(HttpMethod.POST, "/user/api/v1/auth/registrations/1"));
    }

    private boolean publicRequest(HttpMethod method, String path) {
        return GatewaySecurityFilter.isPublic(MockServerHttpRequest.method(method, path).build());
    }
}
