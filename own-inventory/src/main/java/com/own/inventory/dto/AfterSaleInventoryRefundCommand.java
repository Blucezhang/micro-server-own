package com.own.inventory.dto;
import java.util.List;
public class AfterSaleInventoryRefundCommand { private String afterSaleNo; private List<AfterSaleInventoryRefundItem> items; public String getAfterSaleNo(){return afterSaleNo;} public void setAfterSaleNo(String v){afterSaleNo=v;} public List<AfterSaleInventoryRefundItem> getItems(){return items;} public void setItems(List<AfterSaleInventoryRefundItem> v){items=v;} }
