package com.own.inventory.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "inv_stock", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "merchant_id"}))
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected InventoryStock() {
    }

    public InventoryStock(Long productId, Long merchantId, Integer availableQuantity) {
        this.productId = productId;
        this.merchantId = merchantId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.soldQuantity = 0;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void reserve(int quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalStateException("insufficient inventory");
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void commit(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("reserved inventory is inconsistent");
        }
        reservedQuantity -= quantity;
        soldQuantity += quantity;
    }

    public void release(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("reserved inventory is inconsistent");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void refund(int quantity) {
        if (soldQuantity < quantity) {
            throw new IllegalStateException("sold inventory is inconsistent");
        }
        soldQuantity -= quantity;
        availableQuantity += quantity;
    }
    /** A same-SKU exchange returns one sold item and immediately ships its replacement. */
    public void exchange(int quantity) {
        if (quantity <= 0 || soldQuantity < quantity) throw new IllegalStateException("sold inventory is inconsistent");
        // refund(quantity) followed by a replacement shipment leaves all three balances unchanged.
    }

    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Integer getSoldQuantity() { return soldQuantity; }
    public Long getVersion() { return version; }
}
