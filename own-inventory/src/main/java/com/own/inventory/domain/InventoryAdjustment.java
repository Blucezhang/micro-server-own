package com.own.inventory.domain;
import java.util.Date; import javax.persistence.*;
@Entity @Table(name="inventory_adjustment") public class InventoryAdjustment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="product_id",nullable=false) private Long productId; @Column(name="merchant_id",nullable=false) private Long merchantId;
 @Column(name="adjustment_type",nullable=false,length=16) private String adjustmentType; @Column(nullable=false) private Integer quantity;
 @Column(name="before_available",nullable=false) private Integer beforeAvailable; @Column(name="after_available",nullable=false) private Integer afterAvailable;
 @Column(nullable=false,length=255) private String reason; @Column(name="actor_id",nullable=false) private Long actorId; @Column(name="created_at",nullable=false) private Date createdAt;
 protected InventoryAdjustment(){} public InventoryAdjustment(Long productId,Long merchantId,String type,Integer quantity,Integer before,Integer after,String reason,Long actorId){this.productId=productId;this.merchantId=merchantId;this.adjustmentType=type;this.quantity=quantity;this.beforeAvailable=before;this.afterAvailable=after;this.reason=reason;this.actorId=actorId;this.createdAt=new Date();}
 public Long getProductId(){return productId;} public Long getMerchantId(){return merchantId;} public String getAdjustmentType(){return adjustmentType;} public Integer getQuantity(){return quantity;} public Integer getBeforeAvailable(){return beforeAvailable;} public Integer getAfterAvailable(){return afterAvailable;} public String getReason(){return reason;} public Long getActorId(){return actorId;}
}
