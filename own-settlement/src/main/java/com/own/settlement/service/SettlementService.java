package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.PaymentStatus;
import com.own.settlement.domain.PaymentChannel;
import com.own.settlement.domain.Refund;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.dto.CreatePaymentCommand;
import com.own.settlement.dto.MockPaymentCallbackCommand;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final RefundRepository refundRepository;
    private final AfterSaleRefundRepository afterSaleRefundRepository;
    private final String defaultChannel;
    @Autowired(required = false) private MerchantSettlementService merchantSettlementService;
    public SettlementService(PaymentRepository paymentRepository, OrderClient orderClient, RefundRepository refundRepository,
                             AfterSaleRefundRepository afterSaleRefundRepository) {
        this(paymentRepository, orderClient, refundRepository, afterSaleRefundRepository, "MOCK_WECHAT");
    }
    @Autowired
    public SettlementService(PaymentRepository paymentRepository, OrderClient orderClient, RefundRepository refundRepository,
                             AfterSaleRefundRepository afterSaleRefundRepository,
                             @Value("${trade.payment.channel:MOCK_WECHAT}") String defaultChannel) {
        this.paymentRepository = paymentRepository; this.orderClient = orderClient; this.refundRepository = refundRepository;
        this.afterSaleRefundRepository = afterSaleRefundRepository;
        this.defaultChannel = defaultChannel;
    }

    @Transactional
    public Payment createPayment(TradeActor actor, CreatePaymentCommand command, String requestKey) {
        actor.require(ActorType.BUYER);
        Payment existing = paymentRepository.findByRequestKey(requestKey);
        if (existing != null) {
            if (!existing.getBuyerId().equals(actor.getId())) throw TradeException.conflict("idempotency key belongs to another buyer");
            return existing;
        }
        if (command == null || command.getOrderNo() == null || command.getOrderNo().trim().isEmpty()) {
            throw TradeException.badRequest("orderNo is required");
        }
        Payment orderPayment = paymentRepository.findByOrderNo(command.getOrderNo());
        if (orderPayment != null) return orderPayment;
        BigDecimal amount = orderClient.payableAmount(command.getOrderNo(), actor.getId());
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw TradeException.unprocessable("payment amount is invalid");
        return paymentRepository.save(new Payment("PAY-" + UUID.randomUUID().toString(), command.getOrderNo(),
                actor.getId(), amount, requestKey, PaymentChannel.parse(command.getChannel() == null ? defaultChannel : command.getChannel())));
    }

    @Transactional
    public Payment simulateSuccess(TradeActor actor, String paymentNo) {
        actor.require(ActorType.SYSTEM);
        Payment payment = requirePaymentForUpdate(paymentNo);
        return succeed(payment);
    }

    /** Emulates a provider callback after checking the channel-issued reference and paid amount. */
    @Transactional
    public Payment processMockCallback(PaymentChannel channel, MockPaymentCallbackCommand command) {
        if (channel == null || command == null || blank(command.getPaymentNo()) || blank(command.getProviderPaymentNo())
                || command.getAmount() == null || command.getAmount().signum() < 0 || blank(command.getResult())) {
            throw TradeException.badRequest("paymentNo, providerPaymentNo, amount and result are required");
        }
        Payment payment = requirePaymentForUpdate(command.getPaymentNo());
        if (payment.getPaymentChannel() != channel || !payment.getProviderPaymentNo().equals(command.getProviderPaymentNo())) {
            throw TradeException.forbidden("payment callback does not match the payment channel reference");
        }
        if (payment.getAmount().compareTo(command.getAmount()) != 0) throw TradeException.conflict("payment callback amount does not match");
        if ("SUCCESS".equalsIgnoreCase(command.getResult())) return succeed(payment);
        if ("FAILURE".equalsIgnoreCase(command.getResult())) {
            payment.fail();
            return paymentRepository.save(payment);
        }
        throw TradeException.unprocessable("result must be SUCCESS or FAILURE");
    }

    @Transactional
    public Payment simulateFailure(TradeActor actor, String paymentNo) {
        actor.require(ActorType.SYSTEM);
        Payment payment = requirePaymentForUpdate(paymentNo);
        payment.fail();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refund(TradeActor actor, String orderNo) {
        actor.require(ActorType.BUYER);
        Payment payment = paymentRepository.findByOrderNoForUpdate(orderNo);
        if (payment == null || !payment.getBuyerId().equals(actor.getId())) throw TradeException.notFound("payment was not found");
        if (payment.getStatus() == PaymentStatus.REFUNDED) return payment;
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) throw TradeException.conflict("payment cannot be refunded");
        // Let the order state machine prove that this is a requested full refund
        // before changing merchant receivables. This prevents a caller from
        // reversing settlement for an ordinary paid, still-shippable order.
        orderClient.refundSucceeded(orderNo);
        if (merchantSettlementService != null) merchantSettlementService.reverseOrder(orderNo);
        refundRepository.save(new Refund("RFD-" + UUID.randomUUID().toString(), payment));
        payment.refund();
        return paymentRepository.save(payment);
    }

    @Transactional
    public AfterSaleRefund refundAfterSale(TradeActor actor, AfterSaleRefundCommand command) {
        actor.require(ActorType.SYSTEM);
        if (command == null || blank(command.getAfterSaleNo()) || blank(command.getOrderNo())
                || command.getBuyerId() == null || command.getAmount() == null || command.getAmount().signum() <= 0) {
            throw TradeException.unprocessable("afterSaleNo, orderNo, buyerId and a positive amount are required");
        }
        AfterSaleRefund existing = afterSaleRefundRepository.findByAfterSaleNo(command.getAfterSaleNo());
        if (existing != null) {
            if (!existing.getOrderNo().equals(command.getOrderNo()) || existing.getAmount().compareTo(command.getAmount()) != 0) {
                throw TradeException.conflict("after-sale refund does not match its original request");
            }
            orderClient.afterSaleRefundSucceeded(command.getAfterSaleNo());
            return existing;
        }
        // Serialise refund-limit calculation on the payment row: different after-sales for
        // one order must not both observe the same already-refunded amount.
        Payment payment = paymentRepository.findByOrderNoForUpdate(command.getOrderNo());
        if (payment == null || !payment.getBuyerId().equals(command.getBuyerId())) throw TradeException.notFound("payment was not found");
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) throw TradeException.conflict("payment cannot be partially refunded");
        BigDecimal refunded = afterSaleRefundRepository.succeededAmountByPaymentNo(payment.getPaymentNo());
        if (refunded == null) refunded = BigDecimal.ZERO;
        if (refunded.add(command.getAmount()).compareTo(payment.getAmount()) > 0) {
            throw TradeException.conflict("after-sale refunds exceed payment amount");
        }
        AfterSaleRefund refund = afterSaleRefundRepository.save(new AfterSaleRefund("ASR-" + UUID.randomUUID().toString(),
                command.getAfterSaleNo(), payment, command.getAmount()));
        orderClient.afterSaleRefundSucceeded(command.getAfterSaleNo());
        return refund;
    }

    public Payment find(TradeActor actor, String paymentNo) {
        Payment payment = requirePayment(paymentNo);
        if (actor.getType() != ActorType.SYSTEM && !payment.getBuyerId().equals(actor.getId())) {
            throw TradeException.forbidden("actor does not own this payment");
        }
        return payment;
    }

    private Payment requirePayment(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo);
        if (payment == null) throw TradeException.notFound("payment was not found");
        return payment;
    }
    private Payment requirePaymentForUpdate(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNoForUpdate(paymentNo);
        if (payment == null) throw TradeException.notFound("payment was not found");
        return payment;
    }
    private Payment succeed(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.REFUNDED) return payment;
        if (payment.getStatus() != PaymentStatus.CREATED) throw TradeException.conflict("payment cannot succeed from current status");
        orderClient.paymentSucceeded(payment.getOrderNo());
        if (merchantSettlementService != null) merchantSettlementService.recordPaidOrder(payment.getOrderNo(), orderClient.settlementLines(payment.getOrderNo()));
        payment.succeed();
        return paymentRepository.save(payment);
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
