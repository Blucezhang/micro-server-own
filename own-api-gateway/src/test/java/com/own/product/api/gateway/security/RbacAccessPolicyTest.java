package com.own.product.api.gateway.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.own.face.security.JwtPrincipal;
import com.own.face.trade.ActorType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class RbacAccessPolicyTest {
    private final RbacAccessPolicy policy = new RbacAccessPolicy();

    @Test
    public void buyerCannotUseMerchantFulfilmentPath() {
        assertFalse(policy.decide("/order/api/v1/sub-orders/SUB-1/ship", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void merchantCanManageOwnProductAndFulfilOrders() {
        JwtPrincipal merchant = principal(ActorType.MERCHANT, "ROLE_MERCHANT");
        assertTrue(policy.decide("/product/api/v1/merchant/products", "POST", merchant).isAllowed());
        assertTrue(policy.decide("/order/api/v1/sub-orders/SUB-1/ship", "POST", merchant).isAllowed());
        assertTrue(policy.decide("/order/api/v1/sub-orders/SUB-1/shipment-correction", "POST", merchant).isAllowed());
        assertFalse(policy.decide("/order/api/v1/sub-orders/SUB-1/shipment-correction", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void buyerAndMerchantCanReadOwnedLogisticsButOnlyMerchantCanAppendIt() {
        JwtPrincipal buyer = principal(ActorType.BUYER, "ROLE_BUYER");
        JwtPrincipal merchant = principal(ActorType.MERCHANT, "ROLE_MERCHANT");
        String path = "/order/api/v1/sub-orders/SUB-1/logistics-traces";
        assertTrue(policy.decide(path, "GET", buyer).isAllowed());
        assertTrue(policy.decide(path, "GET", merchant).isAllowed());
        assertFalse(policy.decide(path, "POST", buyer).isAllowed());
        assertTrue(policy.decide(path, "POST", merchant).isAllowed());
    }

    @Test
    public void systemOnlyCanReachInternalOperations() {
        assertFalse(policy.decide("/order/api/v1/internal/orders/ORD-1/payment-succeeded", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/order/api/v1/internal/orders/ORD-1/payment-succeeded", "POST", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void buyerCannotOperateLegacyRoleGraph() {
        assertFalse(policy.decide("/user/role/Role", "PUT", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/user/role/Role", "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void onlySystemCanGrantMarketplaceMerchantRole() {
        String path = "/user/api/v1/system/accounts/7/merchant-role";
        assertTrue(policy.decide(path, "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide(path, "PUT", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide(path, "PUT", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void onlySystemCanAppendRoleFunctions() {
        String path = "/user/api/v1/system/roles/7/functions";
        assertTrue(policy.decide(path, "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide(path, "PUT", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void explicitPermissionCanSupplementStandardRole() {
        JwtPrincipal merchant = new JwtPrincipal(1L, 9L, ActorType.MERCHANT, Collections.<String>emptyList(), Arrays.asList("order:fulfill"));
        assertTrue(policy.decide("/order/api/v1/sub-orders/SUB-1/ship", "POST", merchant).isAllowed());
    }

    @Test
    public void buyerCannotWriteLegacyProductEndpoint() {
        assertFalse(policy.decide("/product/addProduct", "PUT", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/product/addProduct", "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void legacyGraphReadsAreSystemOnlyToo() {
        assertFalse(policy.decide("/product/Product/7", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/product/Product/7", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/category/query", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/category/query", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/sale/storeTicket/query/all", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/sale/storeTicket/query/all", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void legacyFileEndpointsAreSystemOnlyWhileOwnedFilesRemainRoleScoped() {
        assertFalse(policy.decide("/file/Picture/secret/jpg", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/file/Picture/secret/jpg", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertTrue(policy.decide("/file/api/v1/files/owned/content", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void legacyNotificationAndWorkflowHistoryAreSystemOnly() {
        assertFalse(policy.decide("/Info/sms", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/flow/workflow", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/Info/sms", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertTrue(policy.decide("/flow/workflow", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void unclassifiedReadsAndCommandsAreLimitedToSystem() {
        assertFalse(policy.decide("/unknown/api/v1/command", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/unknown/api/v1/command", "POST", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/unknown/api/v1/query", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/unknown/api/v1/query", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void paymentDetailsAndMerchantAlertsHaveExplicitReadPolicies() {
        assertTrue(policy.decide("/settlement/api/v1/payments/PAY-1", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/settlement/api/v1/payments/PAY-1", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/settlement/api/v1/payments/PAY-1", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/inventory/api/v1/merchant/low-stock-alerts", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide("/inventory/api/v1/merchant/low-stock-alerts", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void merchantCanProcessAfterSalesButBuyerCannotAuditThem() {
        assertTrue(policy.decide("/order/api/v1/after-sales/AS-1/audit", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide("/order/api/v1/after-sales/AS-1/audit", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/order/api/v1/after-sales/AS-1/exchange-receive", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/order/api/v1/after-sales/AS-1/exchange-receive", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void buyerAndMerchantCanReadAfterSaleDetailButSystemCannotUseTheBuyerMerchantView() {
        String path = "/order/api/v1/after-sales/AS-1";
        assertTrue(policy.decide(path, "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide(path, "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide(path, "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void buyerCanManageOwnAddressButMerchantCannot() {
        assertTrue(policy.decide("/user/api/v1/addresses", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/user/api/v1/addresses", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void buyerAndMerchantCanUseOnlyTheirOwnAccountProfileEndpoint() {
        String path = "/user/api/v1/account/profile";
        assertTrue(policy.decide(path, "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide(path, "PUT", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide(path, "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertTrue(policy.decide("/user/api/v1/account/sessions", "DELETE", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void everyActorCanReadOnlyTheAuthorizationProjectionWhileSystemProfileWritesStayDenied() {
        String path = "/user/api/v1/account/authorization";
        assertTrue(policy.decide(path, "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide(path, "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide(path, "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide(path, "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void onlyMerchantOrSystemCanReadExactInventory() {
        String path = "/inventory/api/v1/stocks/5";
        assertFalse(policy.decide(path, "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide(path, "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide(path, "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
    }

    @Test
    public void buyerAndMerchantCanUseOwnedFiles() {
        assertTrue(policy.decide("/file/api/v1/files", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertTrue(policy.decide("/file/api/v1/files", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void buyerCanManageFavoritesButMerchantCannot() {
        assertTrue(policy.decide("/product/api/v1/favorites/5", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/product/api/v1/favorites/5", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void onlyBuyerCanPublishProductReview() {
        assertTrue(policy.decide("/product/api/v1/products/5/reviews", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/product/api/v1/products/5/reviews", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
    }

    @Test
    public void merchantCanReplyToReviewsAndSystemCanModerateThem() {
        assertTrue(policy.decide("/product/api/v1/merchant/reviews/5/reply", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/product/api/v1/products/5/reviews/9/moderation", "PUT", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/product/api/v1/products/5/reviews/9/moderation", "PUT", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void buyerCanReportReviewButOnlySystemCanInspectReports() {
        assertTrue(policy.decide("/product/api/v1/products/5/reviews/9/reports", "POST", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
        assertFalse(policy.decide("/product/api/v1/products/5/reviews/9/reports", "POST", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertTrue(policy.decide("/product/api/v1/system/review-reports", "GET", principal(ActorType.SYSTEM, "ROLE_SYSTEM")).isAllowed());
        assertFalse(policy.decide("/product/api/v1/system/review-reports", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    @Test
    public void merchantCanReadOwnCouponSummaryButBuyerCannot() {
        assertTrue(policy.decide("/sale/api/v1/coupons/merchant/summary", "GET", principal(ActorType.MERCHANT, "ROLE_MERCHANT")).isAllowed());
        assertFalse(policy.decide("/sale/api/v1/coupons/merchant/summary", "GET", principal(ActorType.BUYER, "ROLE_BUYER")).isAllowed());
    }

    private JwtPrincipal principal(ActorType type, String role) {
        return new JwtPrincipal(1L, 2L, type, Arrays.asList(role), Collections.<String>emptyList());
    }
}
