package com.own.user.party.address.domain;

import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "buyer_address")
public class BuyerAddress {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name="buyer_id", nullable=false) private Long buyerId;
 @Column(name="recipient_name", nullable=false) private String recipientName;
 @Column(nullable=false) private String mobile;
 @Column(nullable=false) private String province;
 @Column(nullable=false) private String city;
 @Column(nullable=false) private String district;
 @Column(nullable=false) private String detail;
 @Column(name="is_default", nullable=false) private Boolean defaultAddress;
 @Column(name="created_at", nullable=false) private Date createdAt;
 @Column(name="updated_at", nullable=false) private Date updatedAt;
 protected BuyerAddress() { }
 public BuyerAddress(Long buyerId, String recipientName, String mobile, String province, String city, String district, String detail, boolean defaultAddress) { this.buyerId=buyerId; update(recipientName,mobile,province,city,district,detail); this.defaultAddress=defaultAddress; this.createdAt=new Date(); }
 public void update(String recipientName,String mobile,String province,String city,String district,String detail) { this.recipientName=recipientName;this.mobile=mobile;this.province=province;this.city=city;this.district=district;this.detail=detail;this.updatedAt=new Date(); }
 public void setDefaultAddress(boolean value) { this.defaultAddress=value; this.updatedAt=new Date(); }
 public String formatted() { return province + city + district + detail; }
 public Long getId(){return id;} public Long getBuyerId(){return buyerId;} public String getRecipientName(){return recipientName;} public String getMobile(){return mobile;} public String getProvince(){return province;} public String getCity(){return city;} public String getDistrict(){return district;} public String getDetail(){return detail;} public Boolean getDefaultAddress(){return defaultAddress;}
}
