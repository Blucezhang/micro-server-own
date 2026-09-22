package com.own.settlement.domain;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import org.junit.Test;

public class PaymentTest {

    @Test
    public void simulatedPaymentCanBeRefundedOnce() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("12.50"), "key");
        payment.succeed();
        payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    @Test
    public void localAlipayPaymentHasChannelSpecificProviderReference() {
        Payment payment = new Payment("PAY-2", "ORD-2", 1L, BigDecimal.ONE, "key-2", PaymentChannel.MOCK_ALIPAY);
        assertEquals(PaymentChannel.MOCK_ALIPAY, payment.getPaymentChannel());
        org.junit.Assert.assertTrue(payment.getProviderPaymentNo().startsWith("ALIPAY-"));
    }
}
