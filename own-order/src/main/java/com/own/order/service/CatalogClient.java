package com.own.order.service;

import com.own.face.trade.TradeException;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;

    public CatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public CatalogProduct getProduct(Long productId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://own-product/product/Product/{productId}", Map.class, productId);
            Map body = response.getBody();
            Object data = body == null ? null : body.get("data");
            if (!(data instanceof Map)) {
                throw TradeException.notFound("product was not found");
            }
            Map product = (Map) data;
            if (product.get("saleStatus") != null && !"AVAILABLE".equals(String.valueOf(product.get("saleStatus")))) {
                throw TradeException.conflict("product is off shelf");
            }
            Long merchantId = longValue(product.get("partyId"), "product merchant");
            String name = String.valueOf(product.get("name"));
            BigDecimal price = price(product.get("promotionPrice"), product.get("originalPrice"));
            return new CatalogProduct(productId, merchantId, name, price);
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.notFound("product catalog is unavailable");
        }
    }

    private Long longValue(Object value, String field) {
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception exception) {
            throw TradeException.unprocessable(field + " is invalid");
        }
    }

    private BigDecimal price(Object promotionPrice, Object originalPrice) {
        String candidate = promotionPrice == null ? "" : String.valueOf(promotionPrice).trim();
        if (candidate.isEmpty() || "0".equals(candidate) || "0.0".equals(candidate)) {
            candidate = originalPrice == null ? "" : String.valueOf(originalPrice).trim();
        }
        try {
            BigDecimal price = new BigDecimal(candidate);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw TradeException.unprocessable("product price must not be negative");
            }
            return price;
        } catch (NumberFormatException exception) {
            throw TradeException.unprocessable("product price is invalid");
        }
    }

    public static final class CatalogProduct {
        private final Long productId;
        private final Long merchantId;
        private final String name;
        private final BigDecimal price;
        CatalogProduct(Long productId, Long merchantId, String name, BigDecimal price) {
            this.productId = productId; this.merchantId = merchantId; this.name = name; this.price = price;
        }
        public Long getProductId() { return productId; }
        public Long getMerchantId() { return merchantId; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }
}
