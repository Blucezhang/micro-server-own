package com.own.inventory.dto;

import java.util.List;

public class InventoryBootstrapRequest {
    private List<InventoryBootstrapItem> products;

    public List<InventoryBootstrapItem> getProducts() { return products; }
    public void setProducts(List<InventoryBootstrapItem> products) { this.products = products; }
}
