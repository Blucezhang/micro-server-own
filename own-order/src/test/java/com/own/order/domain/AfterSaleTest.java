package com.own.order.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
public class AfterSaleTest {
 @Test public void returnAndRefundFollowsMerchantAndReturnFlow(){AfterSale sale=new AfterSale("AS-1","ORD-1","SUB-1",1L,2L,AfterSaleType.RETURN_AND_REFUND,new BigDecimal("20.00"),"damaged");sale.approve("approved");assertEquals(AfterSaleStatus.APPROVED,sale.getStatus());sale.submitReturn("DEMO","T-1");sale.receiveReturn();assertEquals(AfterSaleStatus.REFUND_PENDING,sale.getStatus());sale.refund();assertEquals(AfterSaleStatus.REFUNDED,sale.getStatus());}
 @Test public void refundOnlyBecomesRefundPendingImmediately(){AfterSale sale=new AfterSale("AS-1","ORD-1","SUB-1",1L,2L,AfterSaleType.REFUND_ONLY,BigDecimal.ONE,"missing");sale.approve(null);assertEquals(AfterSaleStatus.REFUND_PENDING,sale.getStatus());}
 @Test public void reminderOnlyRecordsItsTimestampWithoutChangingTheState(){AfterSale sale=new AfterSale("AS-1","ORD-1","SUB-1",1L,2L,AfterSaleType.REFUND_ONLY,BigDecimal.ONE,"missing");sale.remind();assertEquals(AfterSaleStatus.APPLYING,sale.getStatus());assertNotNull(sale.getLastReminderAt());}
 @Test public void buyerCanOnlyCloseBeforeMerchantAudit(){AfterSale sale=new AfterSale("AS-1","ORD-1","SUB-1",1L,2L,AfterSaleType.REFUND_ONLY,BigDecimal.ONE,"missing");sale.close();assertEquals(AfterSaleStatus.CLOSED,sale.getStatus());try{sale.close();fail("closed request cannot be closed twice");}catch(IllegalStateException expected){/* expected */}}
 @Test public void exchangeRequiresBuyerReceiptAfterMerchantShipsReplacement(){AfterSale sale=new AfterSale("AS-1","ORD-1","SUB-1",1L,2L,AfterSaleType.EXCHANGE,BigDecimal.ONE,"wrong size");sale.approve("approved");sale.submitReturn("DEMO","RETURN-1");sale.receiveReturn();sale.exchangeShip("DEMO","REPLACEMENT-1");assertEquals(AfterSaleStatus.EXCHANGE_SHIPPED,sale.getStatus());sale.confirmExchangeReceipt();assertEquals(AfterSaleStatus.EXCHANGED,sale.getStatus());}
}
