package com.own.inventory.domain;
import java.util.Date; import jakarta.persistence.*;
@Entity @Table(name="inv_after_sale_exchange") public class AfterSaleInventoryExchange {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="after_sale_no",nullable=false,length=64) private String afterSaleNo; @Column(name="product_id",nullable=false) private Long productId; @Column(name="merchant_id",nullable=false) private Long merchantId; @Column(nullable=false) private Integer quantity; @Column(name="created_at",nullable=false) private Date createdAt;
 protected AfterSaleInventoryExchange(){} public AfterSaleInventoryExchange(String no,Long productId,Long merchantId,Integer quantity){this.afterSaleNo=no;this.productId=productId;this.merchantId=merchantId;this.quantity=quantity;this.createdAt=new Date();} public Long getMerchantId(){return merchantId;} public Integer getQuantity(){return quantity;}
}
