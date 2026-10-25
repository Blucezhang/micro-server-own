package com.own.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeException;
import com.own.product.domain.Product;
import com.own.product.dto.MerchantProductCommand;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ProductSnapshotCodec {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE =
            new TypeReference<LinkedHashMap<String, Object>>() {};

    private final ObjectMapper objectMapper;

    public ProductSnapshotCodec() {
        this(new ObjectMapper());
    }

    public ProductSnapshotCodec(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public String eventKey(Long merchantId, Long productId, String idempotencyKey) {
        if (merchantId == null || productId == null || idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw TradeException.unprocessable("merchantId, productId and Idempotency-Key are required");
        }
        return sha256Hex(merchantId + ":" + productId + ":" + idempotencyKey.trim());
    }

    public String fingerprint(MerchantProductCommand command) {
        Map<String, Object> canonical = new LinkedHashMap<String, Object>();
        canonical.put("partyId", command == null ? null : command.getPartyId());
        canonical.put("categoryId", command == null ? null : command.getCategoryId());
        canonical.put("brandId", command == null ? null : command.getBrandId());
        canonical.put("name", command == null || command.getName() == null ? null : command.getName().trim());
        canonical.put("content", command == null ? null : command.getContent());
        canonical.put("skuCode", command == null || command.getSkuCode() == null ? null : command.getSkuCode().trim());
        canonical.put("specification", command == null ? null : command.getSpecification());
        canonical.put("originalPrice", command == null ? null : command.getOriginalPrice());
        canonical.put("promotionPrice", normalizePromotionPrice(command == null ? null : command.getPromotionPrice()));
        canonical.put("priceChangeReason", normalizeReason(command == null ? null : command.getPriceChangeReason()));
        try {
            return sha256Hex(objectMapper.writeValueAsString(canonical));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize product command fingerprint", ex);
        }
    }

    public String serializeProduct(Product product) {
        Objects.requireNonNull(product, "product is required");
        Map<String, Object> snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("id", product.getId());
        snapshot.put("name", product.getName());
        snapshot.put("originalPrice", product.getOriginalPrice());
        snapshot.put("promotionPrice", product.getPromotionPrice());
        snapshot.put("stocksNum", product.getStocksNum());
        snapshot.put("salesNum", product.getSalesNum());
        snapshot.put("categoryId", product.getCategoryId());
        snapshot.put("brandId", product.getBrandId());
        snapshot.put("partyId", product.getPartyId());
        snapshot.put("content", product.getContent());
        snapshot.put("skuCode", product.getSkuCode());
        snapshot.put("specification", product.getSpecification());
        snapshot.put("saleStatus", product.getSaleStatus());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize product snapshot", ex);
        }
    }

    public Product deserializeProduct(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.trim().isEmpty()) {
            throw new IllegalStateException("Product snapshot JSON must not be empty");
        }
        try {
            Map<String, Object> map = objectMapper.readValue(snapshotJson, MAP_TYPE);
            Product product = new Product();
            product.setId(asLong(map.get("id")));
            product.setName(asString(map.get("name")));
            product.setOriginalPrice(asString(map.get("originalPrice")));
            product.setPromotionPrice(asString(map.get("promotionPrice")));
            product.setStocksNum(asString(map.get("stocksNum")));
            product.setSalesNum(asString(map.get("salesNum")));
            product.setCategoryId(asLong(map.get("categoryId")));
            product.setBrandId(asLong(map.get("brandId")));
            product.setPartyId(asLong(map.get("partyId")));
            product.setContent(asString(map.get("content")));
            product.setSkuCode(asString(map.get("skuCode")));
            product.setSpecification(asString(map.get("specification")));
            product.setSaleStatus(asString(map.get("saleStatus")));
            return product;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize product snapshot", ex);
        }
    }

    private String normalizePromotionPrice(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private String normalizeReason(String reason) {
        return reason == null ? "" : reason.trim();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append( Character.forDigit((b >> 4) & 0xF, 16) );
                hex.append( Character.forDigit(b & 0xF, 16) );
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
