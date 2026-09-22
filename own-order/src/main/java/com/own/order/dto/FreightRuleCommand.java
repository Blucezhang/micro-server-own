package com.own.order.dto;
import java.math.BigDecimal;
public class FreightRuleCommand { private BigDecimal fixedAmount; private BigDecimal freeThreshold; public BigDecimal getFixedAmount(){return fixedAmount;} public void setFixedAmount(BigDecimal v){fixedAmount=v;} public BigDecimal getFreeThreshold(){return freeThreshold;} public void setFreeThreshold(BigDecimal v){freeThreshold=v;} }
