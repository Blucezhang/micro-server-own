package com.own.inventory.service;

import com.own.face.trade.TradeException;
import com.own.inventory.dto.InventoryBootstrapItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Reads the legacy product catalogue once; it never treats its stock field as live inventory. */
@Component
public class ProductBootstrapClient {

    private final RestTemplate restTemplate;

    public ProductBootstrapClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<InventoryBootstrapItem> loadCatalogue() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("http://own-product/product", Map.class);
            Object data = response.getBody() == null ? null : response.getBody().get("data");
            if (!(data instanceof Iterable)) {
                throw TradeException.conflict("product catalogue did not return a product list");
            }
            List<InventoryBootstrapItem> result = new ArrayList<InventoryBootstrapItem>();
            for (Object candidate : (Iterable<Object>) data) {
                if (!(candidate instanceof Map)) {
                    continue;
                }
                Map<String, Object> product = (Map<String, Object>) candidate;
                InventoryBootstrapItem item = new InventoryBootstrapItem();
                item.setProductId(asLong(product.get("id")));
                item.setMerchantId(asLong(product.get("partyId")));
                Object stocksNum = product.get("stocksNum");
                item.setStocksNum(stocksNum == null ? null : String.valueOf(stocksNum));
                result.add(item);
            }
            if (result.isEmpty()) {
                throw TradeException.conflict("product catalogue contains no bootstrapable products");
            }
            return result;
        } catch (TradeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw TradeException.conflict("product catalogue is unavailable for inventory bootstrap");
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            throw TradeException.unprocessable("catalogue product id and partyId are required");
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw TradeException.unprocessable("catalogue product id and partyId must be numeric");
        }
    }
}
