package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.MerchantReceivable;
import com.own.settlement.domain.MerchantWithdrawal;
import com.own.settlement.domain.SettlementBatch;
import com.own.settlement.dto.MerchantSettlementLine;
import com.own.settlement.repository.MerchantReceivableRepository;
import com.own.settlement.repository.MerchantWithdrawalRepository;
import com.own.settlement.repository.SettlementBatchRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Local 80/20 merchant ledger, weekly settlement and withdrawal reservation. */
@Service
public class MerchantSettlementService {
    private final MerchantReceivableRepository receivables;
    private final SettlementBatchRepository batches;
    private final MerchantWithdrawalRepository withdrawals;
    private final BigDecimal merchantRate;
    private final int cycleDays;
    private final BigDecimal withdrawalMinimum;

    public MerchantSettlementService(MerchantReceivableRepository receivables, SettlementBatchRepository batches,
                                     MerchantWithdrawalRepository withdrawals,
                                     @Value("${trade.settlement.merchant-rate:0.80}") BigDecimal merchantRate,
                                     @Value("${trade.settlement.cycle-days:7}") int cycleDays,
                                     @Value("${trade.settlement.withdrawal-minimum:100.00}") BigDecimal withdrawalMinimum) {
        this.receivables = receivables; this.batches = batches; this.withdrawals = withdrawals;
        if (merchantRate == null || merchantRate.signum() < 0 || merchantRate.compareTo(BigDecimal.ONE) > 0) throw new IllegalArgumentException("merchant rate must be between 0 and 1");
        this.merchantRate = merchantRate; this.cycleDays = cycleDays <= 0 ? 7 : cycleDays; this.withdrawalMinimum = withdrawalMinimum == null ? new BigDecimal("100.00") : withdrawalMinimum;
    }

    @Transactional
    public List<MerchantReceivable> recordPaidOrder(String orderNo, List<MerchantSettlementLine> lines) {
        if (blank(orderNo) || lines == null || lines.isEmpty()) throw TradeException.unprocessable("orderNo and merchant settlement lines are required");
        List<MerchantReceivable> result = new ArrayList<MerchantReceivable>();
        for (MerchantSettlementLine line : lines) {
            if (line == null || line.getMerchantId() == null || line.getMerchantId().longValue() <= 0 || line.getGrossAmount() == null || line.getGrossAmount().signum() < 0) throw TradeException.unprocessable("merchantId and nonnegative grossAmount are required");
            MerchantReceivable existing = receivables.findByOrderNoAndMerchantId(orderNo, line.getMerchantId());
            if (existing != null) {
                if (existing.getGrossAmount().compareTo(line.getGrossAmount()) != 0) throw TradeException.conflict("merchant receivable does not match paid order");
                result.add(existing);
            } else result.add(receivables.save(new MerchantReceivable(orderNo, line.getMerchantId(), line.getGrossAmount(), merchantRate)));
        }
        return result;
    }

    @Transactional
    public SettlementBatch settleDue(Date now) {
        Date current = now == null ? new Date() : now;
        Date cutoff = new Date(current.getTime() - cycleDays * 24L * 60L * 60L * 1000L);
        List<MerchantReceivable> due = receivables.findByStatusAndCreatedAtBefore("PENDING", cutoff);
        if (due.isEmpty()) return null;
        SettlementBatch batch = batches.save(new SettlementBatch("STL-" + UUID.randomUUID().toString(), cutoff, current));
        for (MerchantReceivable receivable : due) receivable.settle(batch.getBatchNo());
        receivables.saveAll(due); return batch;
    }

    @Transactional
    public void reverseOrder(String orderNo) {
        for (MerchantReceivable receivable : receivables.findByOrderNo(orderNo)) {
            if ("PENDING".equals(receivable.getStatus()) || "SETTLED".equals(receivable.getStatus())) receivable.reverse();
        }
    }

    @Transactional
    public MerchantWithdrawal requestWithdrawal(TradeActor actor, BigDecimal amount) {
        actor.require(ActorType.MERCHANT);
        if (amount == null || amount.compareTo(withdrawalMinimum) < 0) throw TradeException.unprocessable("withdrawal amount must be at least " + withdrawalMinimum);
        List<MerchantReceivable> settled = receivables.lockSettledByMerchantId(actor.getId());
        List<MerchantWithdrawal> reserved = withdrawals.lockReservedByMerchantId(actor.getId());
        BigDecimal available = sumReceivables(settled).subtract(sumWithdrawals(reserved));
        if (available.compareTo(amount) < 0) throw TradeException.conflict("insufficient settled merchant balance");
        return withdrawals.save(new MerchantWithdrawal("WTH-" + UUID.randomUUID().toString(), actor.getId(), amount));
    }

    @Transactional
    public MerchantWithdrawal simulateWithdrawalPaid(TradeActor actor, String withdrawalNo) {
        actor.require(ActorType.SYSTEM); MerchantWithdrawal withdrawal = withdrawals.findByWithdrawalNo(withdrawalNo);
        if (withdrawal == null) throw TradeException.notFound("withdrawal was not found"); if ("PAID".equals(withdrawal.getStatus())) return withdrawal;
        withdrawal.pay(); return withdrawals.save(withdrawal);
    }

    @Transactional
    public BigDecimal availableBalance(TradeActor actor) {
        actor.require(ActorType.MERCHANT);
        return sumReceivables(receivables.lockSettledByMerchantId(actor.getId())).subtract(sumWithdrawals(withdrawals.lockReservedByMerchantId(actor.getId())));
    }
    private BigDecimal sumReceivables(List<MerchantReceivable> values) { BigDecimal total = BigDecimal.ZERO; for (MerchantReceivable value : values) total = total.add(value.getMerchantAmount()); return total; }
    private BigDecimal sumWithdrawals(List<MerchantWithdrawal> values) { BigDecimal total = BigDecimal.ZERO; for (MerchantWithdrawal value : values) total = total.add(value.getAmount()); return total; }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
