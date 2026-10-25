package com.own.product.service;

import com.own.face.trade.TradeException;
import com.own.product.dao.ProductDao;
import com.own.product.dao.ProductPriceChangeEventDao;
import com.own.product.domain.Product;
import com.own.product.domain.ProductPriceChangeEvent;
import com.own.product.dto.MerchantProductCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductUpdateGraphTransactionService {

    public static final class Outcome {
        private final Product product;
        private final ProductPriceChangeEvent event;
        private final boolean replayed;

        public Outcome(Product product, ProductPriceChangeEvent event, boolean replayed) {
            this.product = Objects.requireNonNull(product, "product is required");
            this.event = Objects.requireNonNull(event, "event is required");
            this.replayed = replayed;
        }

        public Product getProduct() { return product; }
        public ProductPriceChangeEvent getEvent() { return event; }
        public boolean isReplayed() { return replayed; }
    }

    private final ProductDao productDao;
    private final ProductPriceChangeEventDao eventDao;
    private final ProductSnapshotCodec snapshotCodec;

    @Autowired
    public ProductUpdateGraphTransactionService(
            ProductDao productDao,
            ProductPriceChangeEventDao eventDao,
            ProductSnapshotCodec snapshotCodec) {
        this.productDao = Objects.requireNonNull(productDao, "productDao is required");
        this.eventDao = Objects.requireNonNull(eventDao, "eventDao is required");
        this.snapshotCodec = Objects.requireNonNull(snapshotCodec, "snapshotCodec is required");
    }

    @Transactional(transactionManager = "neo4jTransactionManager")
    public Outcome executeUpdate(Long productId, Long merchantId, String idempotencyKey, MerchantProductCommand command) {
        String eventKey = snapshotCodec.eventKey(merchantId, productId, idempotencyKey);
        Product locked = productDao.lockProductById(productId);
        if (locked == null) {
            throw TradeException.notFound("product was not found");
        }
        if (!merchantId.equals(locked.getPartyId())) {
            throw TradeException.forbidden("merchant does not own product");
        }
        String fingerprint = snapshotCodec.fingerprint(command);
        ProductPriceChangeEvent existing = eventDao.findByEventKey(eventKey);
        if (existing != null) {
            if (!Objects.equals(existing.getRequestFingerprint(), fingerprint)) {
                throw TradeException.conflict("idempotency key already used with different product update request");
            }
            return new Outcome(snapshotCodec.deserializeProduct(existing.getProductSnapshot()), existing, true);
        }

        String reason = validateReason(command);
        BigDecimal originalBefore = parsePrice(locked.getOriginalPrice(), "originalPrice");
        BigDecimal promotionBefore = parseOptionalPrice(locked.getPromotionPrice(), "promotionPrice");
        BigDecimal originalAfter = validateAndApply(locked, command);
        BigDecimal promotionAfter = parseOptionalPrice(locked.getPromotionPrice(), "promotionPrice");

        Product saved = Objects.requireNonNull(productDao.save(locked), "product save returned no result");
        boolean priceChanged = !samePrice(originalBefore, originalAfter) || !samePrice(promotionBefore, promotionAfter);
        String snapshotJson = snapshotCodec.serializeProduct(saved);
        ProductPriceChangeEvent event = new ProductPriceChangeEvent(
                eventKey,
                fingerprint,
                Objects.requireNonNull(saved.getId(), "saved productId is required"),
                merchantId,
                originalBefore.toPlainString(),
                originalAfter.toPlainString(),
                promotionBefore == null ? null : promotionBefore.toPlainString(),
                promotionAfter == null ? null : promotionAfter.toPlainString(),
                reason,
                Instant.now().getEpochSecond(),
                snapshotJson,
                priceChanged,
                !priceChanged);
        ProductPriceChangeEvent savedEvent = Objects.requireNonNull(eventDao.save(event), "event save returned no result");
        return new Outcome(snapshotCodec.deserializeProduct(snapshotJson), savedEvent, false);
    }

    @Transactional(transactionManager = "neo4jTransactionManager")
    public void markProjected(String eventKey) {
        ProductPriceChangeEvent existing = eventDao.findByEventKey(eventKey);
        if (existing == null) {
            throw new IllegalStateException("Price change event not found for eventKey=" + eventKey);
        }
        if (!Boolean.TRUE.equals(existing.getProjected())) {
            existing.setProjected(true);
            existing.setProjectedAtEpochSecond(Instant.now().getEpochSecond());
            eventDao.save(existing);
        }
    }

    @Transactional(transactionManager = "neo4jTransactionManager")
    public Product changeSaleStatus(Long productId, Long merchantId, String value) {
        if (!"AVAILABLE".equals(value) && !"OFF_SHELF".equals(value)) {
            throw TradeException.unprocessable("saleStatus must be AVAILABLE or OFF_SHELF");
        }
        Product product = productDao.lockProductById(productId);
        if (product == null) throw TradeException.notFound("product was not found");
        if (!merchantId.equals(product.getPartyId())) throw TradeException.forbidden("merchant does not own product");
        product.setSaleStatus(value);
        return Objects.requireNonNull(productDao.save(product), "product save returned no result");
    }

    private String validateReason(MerchantProductCommand command) {
        String reason = command == null || command.getPriceChangeReason() == null
                ? ""
                : command.getPriceChangeReason().trim();
        if (reason.length() > 200) {
            throw TradeException.unprocessable("priceChangeReason must not exceed 200 characters");
        }
        return reason;
    }

    private BigDecimal validateAndApply(Product product, MerchantProductCommand command) {
        if (command == null || isBlank(command.getName()) || isBlank(command.getSkuCode())) {
            throw TradeException.unprocessable("name and skuCode are required");
        }
        if (command.getPartyId() != null && !command.getPartyId().equals(product.getPartyId())) {
            throw TradeException.forbidden("merchant cannot transfer product ownership");
        }
        BigDecimal original = parsePrice(command.getOriginalPrice(), "originalPrice");
        BigDecimal promotion = parseOptionalPrice(command.getPromotionPrice(), "promotionPrice");
        if (promotion != null && promotion.compareTo(original) > 0) {
            throw TradeException.unprocessable("promotionPrice cannot be greater than originalPrice");
        }
        product.setName(command.getName().trim());
        product.setContent(command.getContent());
        product.setSkuCode(command.getSkuCode().trim());
        product.setSpecification(command.getSpecification());
        product.setOriginalPrice(command.getOriginalPrice());
        product.setPromotionPrice(command.getPromotionPrice());
        product.setCategoryId(command.getCategoryId());
        product.setBrandId(command.getBrandId());
        return original;
    }

    public static BigDecimal parseOptionalPrice(String value, String name) {
        return value == null || value.trim().isEmpty() ? null : parsePrice(value, name);
    }

    public static BigDecimal parsePrice(String value, String name) {
        try {
            BigDecimal amount = new BigDecimal(value);
            if (amount.signum() < 0) {
                throw TradeException.unprocessable(name + " must not be negative");
            }
            if (amount.stripTrailingZeros().scale() > 2) {
                throw TradeException.unprocessable(name + " must be an amount in cents");
            }
            return amount;
        } catch (TradeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw TradeException.unprocessable(name + " is invalid");
        }
    }

    public static boolean samePrice(BigDecimal left, BigDecimal right) {
        return left == null ? right == null : right != null && left.compareTo(right) == 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
