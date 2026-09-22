package com.own.product.api.gateway.security;

import com.own.face.security.JwtPrincipal;
import com.own.face.trade.ActorType;
import java.util.List;

/**
 * Small, explicit edge policy for the marketplace APIs.  Domain services still
 * verify ownership; this policy only decides which authenticated actor can
 * reach a category of command.  A legacy function graph permission may grant
 * the matching action in addition to the standard marketplace role.
 */
final class RbacAccessPolicy {

    Decision decide(String path, String method, JwtPrincipal principal) {
        String value = path == null ? "" : path.toLowerCase();
        String verb = method == null ? "GET" : method.toUpperCase();
        if (value.contains("/internal/")) return require(principal, ActorType.SYSTEM, "system:operate");
        if (value.startsWith("/user/role/") || value.startsWith("/user/fun/") || value.startsWith("/user/org/") ||
                value.startsWith("/user/login/loginuser") || value.startsWith("/user/login/getloginuser") ||
                (value.startsWith("/user/per/person") && !value.endsWith("/reg"))) {
            return require(principal, ActorType.SYSTEM, "user:manage");
        }
        if (value.startsWith("/user/login/sessions")) return requireBuyerOrMerchant(principal, "auth:session");
        if (value.startsWith("/user/api/v1/system/accounts/")) return require(principal, ActorType.SYSTEM, "user:manage");
        if (value.startsWith("/user/api/v1/system/roles/")) return require(principal, ActorType.SYSTEM, "user:manage");
        if (value.startsWith("/user/api/v1/account/")) return requireBuyerOrMerchant(principal, "account:manage");
        if (value.contains("/product/api/v1/system/")) return require(principal, ActorType.SYSTEM, "product:moderate");
        if (value.contains("/product/api/v1/merchant/")) return require(principal, ActorType.MERCHANT, "product:manage");
        if (value.contains("/product/api/v1/favorites")) return require(principal, ActorType.BUYER, "product:favorite");
        if (value.contains("/product/api/v1/products/") && value.contains("/reviews/") && value.endsWith("/reports")) return require(principal, ActorType.BUYER, "product:review");
        if (value.contains("/product/api/v1/products/") && value.endsWith("/reviews")) return require(principal, ActorType.BUYER, "product:review");
        // Pre-marketplace graph endpoints lack marketplace ownership checks.
        // They are retained only for system migration/maintenance; even reads
        // can expose raw graph metadata, so they must not be role-readable.
        if (value.startsWith("/product/") && !value.startsWith("/product/api/v1/")) {
            return require(principal, ActorType.SYSTEM, "product:legacy-manage");
        }
        if (value.startsWith("/category/")) {
            return require(principal, ActorType.SYSTEM, "category:legacy-manage");
        }
        // Legacy promotion tickets/scopes are graph-maintenance APIs without
        // merchant ownership semantics.  New marketplace coupon APIs are /api/v1.
        if (value.startsWith("/sale/") && !value.startsWith("/sale/api/v1/")) {
            return require(principal, ActorType.SYSTEM, "promotion:legacy-manage");
        }
        // Legacy message and workflow APIs expose operational history without
        // per-recipient/resource ownership. Keep them as maintenance-only paths.
        if (value.startsWith("/info/") || value.equals("/info") || value.startsWith("/flow/")) {
            return require(principal, ActorType.SYSTEM, "operations:legacy-manage");
        }
        if (value.contains("/inventory/api/v1/stocks/") &&
                (value.contains("/adjustments") || value.contains("/low-stock-alert-rule"))) {
            return require(principal, ActorType.MERCHANT, "inventory:manage");
        }
        if ("GET".equals(verb) && value.matches("^/inventory/api/v1/stocks/[^/]+/?$")) {
            return requireInventoryViewer(principal);
        }
        if (value.contains("/settlement/api/v1/merchant/settlement/")) return require(principal, ActorType.MERCHANT, "settlement:withdraw");
        if (value.contains("/settlement/api/v1/payments/") && value.contains("/simulate-")) return require(principal, ActorType.SYSTEM, "payment:operate");
        if (value.contains("/settlement/api/v1/payments") && "POST".equals(verb)) return require(principal, ActorType.BUYER, "payment:create");
        if (value.contains("/file/api/v1/files")) return requireBuyerOrMerchant(principal, "file:manage");
        if (value.startsWith("/file/")) return require(principal, ActorType.SYSTEM, "file:legacy-manage");
        if (value.contains("/user/api/v1/addresses")) return require(principal, ActorType.BUYER, "address:manage");
        if (value.contains("/sale/api/v1/coupons/merchant/")) return require(principal, ActorType.MERCHANT, "coupon:operate");
        if (value.contains("/sale/api/v1/coupons")) return require(principal, ActorType.BUYER, "coupon:claim");
        if (value.contains("/order/api/v1/after-sales/merchant") ||
                (value.contains("/order/api/v1/after-sales/") &&
                        (value.endsWith("/audit") || value.endsWith("/receive-return") || value.endsWith("/exchange-shipment")))) {
            return require(principal, ActorType.MERCHANT, "after-sale:manage");
        }
        if ("GET".equals(verb) && value.matches("^/order/api/v1/after-sales/[^/]+/?$")) {
            return requireAfterSaleParticipant(principal);
        }
        if (value.contains("/order/api/v1/sub-orders/") && value.endsWith("/logistics-traces")) {
            if ("GET".equals(verb)) return requireOrderParticipant(principal);
            return require(principal, ActorType.MERCHANT, "order:fulfill");
        }
        if (value.contains("/order/api/v1/merchant/") ||
                (value.contains("/order/api/v1/sub-orders/") && (value.endsWith("/ship") || value.endsWith("/shipment-correction")))) {
            return require(principal, ActorType.MERCHANT, "order:fulfill");
        }
        if (value.contains("/order/api/v1/cart/") || value.contains("/order/api/v1/checkouts/") ||
                value.contains("/order/api/v1/orders") || value.contains("/order/api/v1/after-sales") ||
                (value.contains("/order/api/v1/sub-orders/") && value.endsWith("/receive"))) {
            return require(principal, ActorType.BUYER, "order:purchase");
        }
        // Do not turn a newly added, unclassified command into an authenticated
        // but unrestricted endpoint.  Read endpoints remain compatible; legacy
        // maintenance commands remain available to SYSTEM until they are moved
        // to a versioned, domain-specific policy.
        return "GET".equals(verb) ? Decision.allowed() : require(principal, ActorType.SYSTEM, "system:operate");
    }

    private Decision require(JwtPrincipal principal, ActorType actorType, String permission) {
        if (principal.getActorType() != actorType) return Decision.denied();
        String role = "ROLE_" + actorType.name();
        if (contains(principal.getRoles(), role) || contains(principal.getPermissions(), permission) || contains(principal.getPermissions(), "*")) return Decision.allowed();
        return Decision.denied();
    }

    private Decision requireBuyerOrMerchant(JwtPrincipal principal, String permission) {
        if (principal.getActorType() != ActorType.BUYER && principal.getActorType() != ActorType.MERCHANT) return Decision.denied();
        String role = "ROLE_" + principal.getActorType().name();
        if (contains(principal.getRoles(), role) || contains(principal.getPermissions(), permission) || contains(principal.getPermissions(), "*")) return Decision.allowed();
        return Decision.denied();
    }

    /** A logistics read is safe only after the order service verifies ownership. */
    private Decision requireOrderParticipant(JwtPrincipal principal) {
        if (principal.getActorType() == ActorType.BUYER) return require(principal, ActorType.BUYER, "order:purchase");
        if (principal.getActorType() == ActorType.MERCHANT) return require(principal, ActorType.MERCHANT, "order:fulfill");
        return Decision.denied();
    }

    private Decision requireAfterSaleParticipant(JwtPrincipal principal) {
        if (principal.getActorType() == ActorType.BUYER) return require(principal, ActorType.BUYER, "order:purchase");
        if (principal.getActorType() == ActorType.MERCHANT) return require(principal, ActorType.MERCHANT, "after-sale:manage");
        return Decision.denied();
    }

    private Decision requireInventoryViewer(JwtPrincipal principal) {
        if (principal.getActorType() == ActorType.MERCHANT) return require(principal, ActorType.MERCHANT, "inventory:manage");
        if (principal.getActorType() == ActorType.SYSTEM) return require(principal, ActorType.SYSTEM, "system:operate");
        return Decision.denied();
    }

    private boolean contains(List<String> values, String expected) {
        if (values == null) return false;
        for (String value : values) if (expected.equalsIgnoreCase(value == null ? "" : value.trim())) return true;
        return false;
    }

    static final class Decision {
        private final boolean allowed;
        private Decision(boolean allowed) { this.allowed = allowed; }
        static Decision allowed() { return new Decision(true); }
        static Decision denied() { return new Decision(false); }
        boolean isAllowed() { return allowed; }
    }
}
