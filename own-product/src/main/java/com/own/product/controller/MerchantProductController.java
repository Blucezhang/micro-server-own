package com.own.product.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.audit.domain.ProductPriceAudit;
import com.own.product.audit.repository.ProductPriceAuditRepository;
import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.dto.MerchantProductCommand;
import com.own.product.dto.PublicProductView;
import com.own.product.service.MerchantProductUpdateService;
import com.own.product.service.ProductUpdateGraphTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/api/v1")
public class MerchantProductController extends BaseController {

    private final ProductDao dao;
    private final ProductPriceAuditRepository priceAudits;
    private final MerchantProductUpdateService updateService;

    @Autowired
    public MerchantProductController(
            ProductDao dao,
            ProductPriceAuditRepository priceAudits,
            MerchantProductUpdateService updateService) {
        this.dao = Objects.requireNonNull(dao, "dao is required");
        this.priceAudits = Objects.requireNonNull(priceAudits, "priceAudits is required");
        this.updateService = Objects.requireNonNull(updateService, "updateService is required");
    }

    @PostMapping("/merchant/products")
    public Resp create(@RequestBody MerchantProductCommand c, HttpServletRequest r) {
        TradeActor a = merchant(r);
        TradeHeaders.idempotencyKey(r);
        if (c == null || c.getPartyId() == null || !a.getId().equals(c.getPartyId())) {
            throw TradeException.forbidden("merchant can only create own product");
        }
        Product p = new Product();
        applyCreate(p, c);
        p.setSaleStatus("AVAILABLE");
        return new Resp(dao.save(p), 201, "created");
    }

    @PutMapping("/merchant/products/{id}")
    public Resp update(@PathVariable Long id, @RequestBody MerchantProductCommand c, HttpServletRequest r) {
        TradeActor a = merchant(r);
        String idempotencyKey = TradeHeaders.idempotencyKey(r);
        return new Resp(updateService.updateProduct(id, a.getId(), idempotencyKey, c));
    }

    @PostMapping("/merchant/products/{id}/sale-status")
    public Resp saleStatus(@PathVariable Long id, @RequestParam String value, HttpServletRequest r) {
        TradeActor a = merchant(r);
        TradeHeaders.idempotencyKey(r);
        if (!"AVAILABLE".equals(value) && !"OFF_SHELF".equals(value)) {
            throw TradeException.unprocessable("saleStatus must be AVAILABLE or OFF_SHELF");
        }
        return new Resp(updateService.changeSaleStatus(id, a.getId(), value));
    }

    @GetMapping("/products")
    public Resp browse(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Product> matched = new ArrayList<Product>();
        for (Product p : dao.queryProduct()) {
            if ("AVAILABLE".equals(status(p)) && matches(p, merchantId, categoryId, keyword, null)) {
                matched.add(p);
            }
        }
        return new Resp(publicPage(matched, page, size));
    }

    @GetMapping("/products/{id}")
    public Resp detail(@PathVariable Long id) {
        Product product = dao.queryProductById(id);
        if (product == null || !"AVAILABLE".equals(status(product))) {
            throw TradeException.notFound("available product was not found");
        }
        return new Resp(new PublicProductView(product));
    }

    @GetMapping("/merchant/products")
    public Resp mine(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String saleStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        TradeActor actor = merchant(request);
        if (saleStatus != null && !"AVAILABLE".equals(saleStatus) && !"OFF_SHELF".equals(saleStatus)) {
            throw TradeException.unprocessable("saleStatus must be AVAILABLE or OFF_SHELF");
        }
        List<Product> matched = new ArrayList<Product>();
        for (Product p : dao.queryProduct()) {
            if (matches(p, actor.getId(), categoryId, keyword, saleStatus)) {
                matched.add(p);
            }
        }
        return new Resp(page(matched, page, size));
    }

    @GetMapping("/merchant/products/{id}/price-audits")
    public Resp priceAudits(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        TradeActor actor = merchant(request);
        owned(id, actor.getId());
        if (page < 0 || size < 1 || size > 100) {
            throw TradeException.unprocessable("page must be nonnegative and size must be 1..100");
        }
        Page<ProductPriceAudit> result =
                priceAudits.findByProductIdAndMerchantIdOrderByIdDesc(id, actor.getId(), PageRequest.of(page, size));
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", result.getTotalElements());
        data.put("page", page);
        data.put("size", size);
        data.put("items", result.getContent());
        return new Resp(data);
    }

    private boolean matches(Product p, Long merchantId, Long categoryId, String keyword, String saleStatus) {
        return (merchantId == null || merchantId.equals(p.getPartyId()))
                && (categoryId == null || categoryId.equals(p.getCategoryId()))
                && (saleStatus == null || saleStatus.equals(status(p)))
                && (keyword == null
                        || p.getName() != null
                                && p.getName().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)));
    }

    private Map<String, Object> page(List<Product> matched, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || page > Integer.MAX_VALUE / size) {
            throw TradeException.unprocessable("page must be nonnegative and size must be 1..100 without overflow");
        }
        Collections.sort(matched, new Comparator<Product>() {
            public int compare(Product left, Product right) {
                Long a = left.getId(), b = right.getId();
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                return a.compareTo(b);
            }
        });
        int from = Math.min(page * size, matched.size());
        int to = Math.min(from + size, matched.size());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", matched.size());
        data.put("page", page);
        data.put("size", size);
        data.put("items", matched.subList(from, to));
        return data;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> publicPage(List<Product> matched, int page, int size) {
        Map<String, Object> data = page(matched, page, size);
        List<PublicProductView> views = new ArrayList<PublicProductView>();
        for (Product product : (List<Product>) data.get("items")) {
            views.add(new PublicProductView(product));
        }
        data.put("items", views);
        return data;
    }

    private TradeActor merchant(HttpServletRequest r) {
        TradeActor a = TradeHeaders.actor(r);
        a.require(ActorType.MERCHANT);
        return a;
    }

    private Product owned(Long id, Long merchantId) {
        Product p = dao.queryProductById(id);
        if (p == null) {
            throw TradeException.notFound("product was not found");
        }
        if (!merchantId.equals(p.getPartyId())) {
            throw TradeException.forbidden("merchant does not own product");
        }
        return p;
    }

    private String status(Product p) {
        return p.getSaleStatus() == null ? "AVAILABLE" : p.getSaleStatus();
    }

    private void applyCreate(Product p, MerchantProductCommand c) {
        if (c == null || blank(c.getName()) || blank(c.getSkuCode())) {
            throw TradeException.unprocessable("name and skuCode are required");
        }
        BigDecimal original = ProductUpdateGraphTransactionService.parsePrice(c.getOriginalPrice(), "originalPrice");
        BigDecimal promotion = ProductUpdateGraphTransactionService.parseOptionalPrice(c.getPromotionPrice(), "promotionPrice");
        if (promotion != null && promotion.compareTo(original) > 0) {
            throw TradeException.unprocessable("promotionPrice cannot be greater than originalPrice");
        }
        p.setName(c.getName().trim());
        p.setContent(c.getContent());
        p.setSkuCode(c.getSkuCode().trim());
        p.setSpecification(c.getSpecification());
        p.setOriginalPrice(c.getOriginalPrice());
        p.setPromotionPrice(c.getPromotionPrice());
        p.setPartyId(c.getPartyId());
        p.setCategoryId(c.getCategoryId());
        p.setBrandId(c.getBrandId());
    }

    private boolean blank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
