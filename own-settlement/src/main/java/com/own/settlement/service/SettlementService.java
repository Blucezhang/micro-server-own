package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.PaymentStatus;
import com.own.settlement.domain.PaymentChannel;
import com.own.settlement.domain.Refund;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.domain.RecoveryAction;
import com.own.face.trade.OrderRecoverySnapshot;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.dto.CreatePaymentCommand;
import com.own.settlement.dto.MockPaymentCallbackCommand;
import com.own.settlement.dto.MerchantSettlementLine;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SettlementService {
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final RefundRepository refundRepository;
    private final AfterSaleRefundRepository afterSaleRefundRepository;
    private final String defaultChannel;
    @Autowired private MerchantSettlementService merchantSettlementService;
    @Autowired private SettlementRecoveryJournal recoveryJournal;
    public SettlementService(PaymentRepository paymentRepository, OrderClient orderClient, RefundRepository refundRepository,
                             AfterSaleRefundRepository afterSaleRefundRepository) {
        this(paymentRepository, orderClient, refundRepository, afterSaleRefundRepository, "MOCK_WECHAT");
    }
    @Autowired
    public SettlementService(PaymentRepository paymentRepository, OrderClient orderClient, RefundRepository refundRepository,
                             AfterSaleRefundRepository afterSaleRefundRepository,
                             @Value("${trade.payment.channel}") String defaultChannel) {
        this.paymentRepository = paymentRepository; this.orderClient = orderClient; this.refundRepository = refundRepository;
        this.afterSaleRefundRepository = afterSaleRefundRepository;
        this.defaultChannel = defaultChannel;
    }

    @Transactional
    public Payment createPayment(TradeActor actor, CreatePaymentCommand command, String requestKey) {
        actor.require(ActorType.BUYER);
        if (command == null || command.getOrderNo() == null || command.getOrderNo().trim().isEmpty()) {
            throw TradeException.badRequest("orderNo is required");
        }
        PaymentChannel channel = PaymentChannel.parse(command.getChannel() == null ? defaultChannel : command.getChannel());
        Payment existing = paymentRepository.findByRequestKey(requestKey);
        if (existing != null) {
            if (!existing.getBuyerId().equals(actor.getId())) throw TradeException.conflict("idempotency key belongs to another buyer");
            if (!existing.getOrderNo().equals(command.getOrderNo()) || existing.getPaymentChannel() != channel) {
                throw TradeException.conflict("idempotency key belongs to another payment request");
            }
            return existing;
        }
        Payment orderPayment = paymentRepository.findByOrderNo(command.getOrderNo());
        if (orderPayment != null) {
            if (!orderPayment.getBuyerId().equals(actor.getId())) throw TradeException.notFound("payment was not found");
            if (orderPayment.getPaymentChannel() != channel) throw TradeException.conflict("payment channel does not match existing order payment");
            return orderPayment;
        }
        BigDecimal amount = orderClient.payableAmount(command.getOrderNo(), actor.getId());
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw TradeException.unprocessable("payment amount is invalid");
        return paymentRepository.save(new Payment("PAY-" + UUID.randomUUID().toString(), command.getOrderNo(),
                actor.getId(), amount, requestKey, channel));
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
            rejectFailureDuringRecovery(payment);
            payment.fail();
            return paymentRepository.save(payment);
        }
        throw TradeException.unprocessable("result must be SUCCESS or FAILURE");
    }

    @Transactional
    public Payment simulateFailure(TradeActor actor, String paymentNo) {
        actor.require(ActorType.SYSTEM);
        Payment payment = requirePaymentForUpdate(paymentNo);
        rejectFailureDuringRecovery(payment);
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
        int attempt = beginRecovery(payment, RecoveryAction.REFUND);
        OrderRecoverySnapshot snapshot = attempt > 1 ? checkedRecoverySnapshot(payment) : null;
        if (snapshot == null || !snapshot.refundCompleted()) {
            if (snapshot != null && !"REFUNDING".equals(snapshot.status())) throw TradeException.conflict("order is not awaiting refund");
            orderClient.refundSucceeded(orderNo);
        }
        if (merchantSettlementService != null) merchantSettlementService.reverseOrder(orderNo);
        refundRepository.save(new Refund("RFD-" + UUID.randomUUID().toString(), payment));
        payment.refund();
        Payment saved = paymentRepository.save(payment);
        recoveryJournal.complete(payment, RecoveryAction.REFUND);
        return saved;
    }

    @Transactional
    public AfterSaleRefund refundAfterSale(TradeActor actor, AfterSaleRefundCommand command) {
        actor.require(ActorType.SYSTEM);
        if (command == null || blank(command.getAfterSaleNo()) || blank(command.getOrderNo())
                || command.getBuyerId() == null || command.getBuyerId() <= 0
                || command.getMerchantId() == null || command.getMerchantId() <= 0
                || command.getAmount() == null || command.getAmount().signum() <= 0) {
            throw TradeException.unprocessable("afterSaleNo, orderNo, buyerId, merchantId and a positive amount are required");
        }
        if (command.getAmount().stripTrailingZeros().scale() > 2) {
            throw TradeException.unprocessable("after-sale refund amount must be an amount in cents");
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("after-sale refund requires an active transaction with synchronization");
        }
        // Lock before the first refund read: a waiting retry must see its predecessor's
        // committed ledger rather than retain a pre-lock snapshot of an absent refund.
        Payment payment = paymentRepository.findByOrderNoForUpdate(command.getOrderNo());
        if (payment == null || !payment.getBuyerId().equals(command.getBuyerId())) throw TradeException.notFound("payment was not found");
        AfterSaleRefund existing = afterSaleRefundRepository.findByAfterSaleNo(command.getAfterSaleNo());
        if (existing != null) {
            if (!existing.getOrderNo().equals(command.getOrderNo()) || existing.getAmount().compareTo(command.getAmount()) != 0
                    || !existing.getPaymentNo().equals(payment.getPaymentNo())
                    || (existing.getMerchantId() != null && !existing.getMerchantId().equals(command.getMerchantId()))) {
                throw TradeException.conflict("after-sale refund does not match its original request");
            }
            orderClient.afterSaleRefundSucceeded(command.getAfterSaleNo());
            return existing;
        }
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) throw TradeException.conflict("payment cannot be partially refunded");
        BigDecimal refunded = afterSaleRefundRepository.succeededAmountByPaymentNo(payment.getPaymentNo());
        if (refunded == null) refunded = BigDecimal.ZERO;
        if (refunded.add(command.getAmount()).compareTo(payment.getAmount()) > 0) {
            throw TradeException.conflict("after-sale refunds exceed payment amount");
        }
        AfterSaleRefund refund = new AfterSaleRefund("ASR-" + UUID.randomUUID().toString(),
                command.getAfterSaleNo(), payment, command.getMerchantId(), command.getAmount());
        merchantSettlementService.allocateAfterSaleRefund(refund);
        refund = afterSaleRefundRepository.save(refund);
        final String afterSaleNo = command.getAfterSaleNo();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                orderClient.afterSaleRefundSucceeded(afterSaleNo);
            }
        });
        return refund;
    }

    public Payment find(TradeActor actor, String paymentNo) {
        Payment payment = requirePayment(paymentNo);
        if (actor.getType() != ActorType.SYSTEM) {
            actor.require(ActorType.BUYER);
            if (!payment.getBuyerId().equals(actor.getId())) {
                throw TradeException.forbidden("actor does not own this payment");
            }
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
        int attempt = beginRecovery(payment, RecoveryAction.PAYMENT);
        List<MerchantSettlementLine> lines = merchantSettlementService == null ? null : checkedSettlementLines(payment);
        OrderRecoverySnapshot snapshot = attempt > 1 ? checkedRecoverySnapshot(payment) : null;
        if (snapshot == null || !snapshot.paymentCompleted()) {
            if (snapshot != null && (!"PENDING_PAYMENT".equals(snapshot.status())
                    || !java.util.Set.of(com.own.face.trade.recovery.RecoveryStatus.NONE, com.own.face.trade.recovery.RecoveryStatus.PENDING).contains(snapshot.paymentRecoveryStatus()))) {
                throw TradeException.conflict("order payment requires reconciliation");
            }
            orderClient.paymentSucceeded(payment.getOrderNo());
        }
        if (merchantSettlementService != null) merchantSettlementService.recordPaidOrder(payment.getOrderNo(), lines);
        payment.succeed();
        Payment saved = paymentRepository.save(payment);
        recoveryJournal.complete(payment, RecoveryAction.PAYMENT);
        return saved;
    }

    @Transactional
    public void recover(String paymentNo, RecoveryAction action) {
        Payment payment = requirePaymentForUpdate(paymentNo);
        if (action == RecoveryAction.PAYMENT) {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.REFUNDED) recoveryJournal.complete(payment, action);
            else succeed(payment);
        } else {
            if (payment.getStatus() == PaymentStatus.REFUNDED) recoveryJournal.complete(payment, action);
            else refund(new TradeActor(payment.getBuyerId(), ActorType.BUYER), payment.getOrderNo());
        }
    }

    private int beginRecovery(Payment payment, RecoveryAction action) {
        int attempt = recoveryJournal.begin(payment, action);
        if (attempt < 1) throw TradeException.conflict("operation requires manual reconciliation");
        return attempt;
    }
    private OrderRecoverySnapshot checkedRecoverySnapshot(Payment payment) {
        OrderRecoverySnapshot snapshot = orderClient.recoverySnapshot(payment.getOrderNo());
        if (snapshot == null || !payment.getOrderNo().equals(snapshot.orderNo()) || !payment.getBuyerId().equals(snapshot.buyerId())
                || snapshot.payableAmount() == null || payment.getAmount().compareTo(snapshot.payableAmount()) != 0) {
            throw TradeException.conflict("order recovery snapshot does not match payment");
        }
        return snapshot;
    }
    private void rejectFailureDuringRecovery(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CREATED && recoveryJournal.hasUnresolvedPayment(payment.getPaymentNo())) {
            throw TradeException.conflict("payment success is being reconciled");
        }
    }

    private List<MerchantSettlementLine> checkedSettlementLines(Payment payment) {
        List<MerchantSettlementLine> lines = orderClient.settlementLines(payment.getOrderNo());
        if (lines == null || lines.isEmpty()) throw TradeException.conflict("order settlement lines are unavailable");
        Set<Long> merchants = new HashSet<Long>();
        BigDecimal total = BigDecimal.ZERO;
        for (MerchantSettlementLine line : lines) {
            if (line == null || line.getMerchantId() == null || line.getMerchantId() <= 0
                    || line.getGrossAmount() == null || line.getGrossAmount().signum() < 0
                    || line.getGrossAmount().stripTrailingZeros().scale() > 2
                    || !merchants.add(line.getMerchantId())) {
                throw TradeException.conflict("invalid order settlement line");
            }
            total = total.add(line.getGrossAmount());
        }
        if (total.compareTo(payment.getAmount()) != 0) {
            throw TradeException.conflict("merchant settlement total does not match payment amount");
        }
        return lines;
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
