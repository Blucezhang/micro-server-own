package com.own.product.api.gateway.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.ActorType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class JwtAuthenticationFilterTest {

    @After
    public void clearRequestContext() {
        RequestContext.getCurrentContext().clear();
    }

    @Test
    public void onlyVersionedStorefrontProductBrowseAndDetailAreAnonymous() {
        MockHttpServletRequest storefront = new MockHttpServletRequest("GET", "/product/api/v1/products");
        MockHttpServletRequest detail = new MockHttpServletRequest("GET", "/product/api/v1/products/7");
        MockHttpServletRequest categories = new MockHttpServletRequest("GET", "/product/api/v1/categories");
        MockHttpServletRequest legacy = new MockHttpServletRequest("GET", "/product?partyId=1");
        assertTrue(JwtAuthenticationFilter.isPublic(storefront));
        assertTrue(JwtAuthenticationFilter.isPublic(detail));
        assertTrue(JwtAuthenticationFilter.isPublic(categories));
        assertFalse(JwtAuthenticationFilter.isPublic(legacy));
    }

    @Test
    public void onlyExactPublicReviewCollectionIsAnonymous() {
        assertTrue(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("GET", "/product/api/v1/products/7/reviews")));
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("GET", "/product/api/v1/products/7/reviews/9")));
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("GET", "/product/api/v1/products/7/internal-data")));
    }

    @Test
    public void passwordChangeRemainsPublicBecauseItVerifiesCurrentPassword() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/login/updatePassword");
        assertTrue(JwtAuthenticationFilter.isPublic(request));
    }

    @Test
    public void publicAuthenticationRoutesMustMatchTheirExactGatewayPath() {
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/other/user/login/token")));
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/login/token/extra")));
        assertTrue(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/login/token")));
    }

    @Test
    public void refreshAndLogoutAcceptRefreshSessionWithoutAccessJwt() {
        assertTrue(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/login/refresh")));
        assertTrue(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/login/logout")));
    }

    @Test
    public void buyerRegistrationIsTheOnlyAnonymousMarketplaceWrite() {
        assertTrue(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/api/v1/auth/registrations")));
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("GET", "/user/api/v1/auth/registrations")));
        assertFalse(JwtAuthenticationFilter.isPublic(new MockHttpServletRequest("POST", "/user/api/v1/auth/registrations/1")));
    }

    @Test
    public void authenticatedRequestPreservesNonIdentityMultiValueHeaders() {
        JwtTokenService tokenService = new JwtTokenService(new ObjectMapper(),
                "01234567890123456789012345678901", 600L, "test");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/order/api/v1/orders");
        request.addHeader("Accept", "application/json");
        request.addHeader("Accept", "application/problem+json");
        request.addHeader("Authorization", "Bearer " + tokenService.issue(1L, 2L, ActorType.BUYER,
                Arrays.asList("ROLE_BUYER"), Collections.<String>emptyList()));
        RequestContext context = RequestContext.getCurrentContext();
        context.setRequest(request);

        new JwtAuthenticationFilter(tokenService, true).run();

        assertEquals(Arrays.asList("application/json", "application/problem+json"),
                Collections.list(context.getRequest().getHeaders("Accept")));
        assertEquals(Collections.singletonList("2"),
                Collections.list(context.getRequest().getHeaders("X-Actor-Id")));
    }
}
