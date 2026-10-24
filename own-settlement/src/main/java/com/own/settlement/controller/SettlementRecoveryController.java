package com.own.settlement.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.settlement.repository.RecoveryOperationRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/internal/recovery")
public class SettlementRecoveryController extends BaseController {
    private final RecoveryOperationRepository operations;
    private final InternalServiceGuard guard;
    public SettlementRecoveryController(RecoveryOperationRepository operations, InternalServiceGuard guard) {
        this.operations = operations; this.guard = guard;
    }
    @GetMapping("/operations")
    @Transactional(readOnly = true)
    public Resp operations(@RequestParam(defaultValue = "RECONCILIATION_REQUIRED") String status, HttpServletRequest request) {
        guard.require(request); TradeHeaders.actor(request).require(ActorType.SYSTEM);
        if (!Set.of("PENDING", "COMPLETED", "RECONCILIATION_REQUIRED").contains(status)) throw TradeException.unprocessable("invalid recovery status");
        return new Resp(operations.findTop100ByStatusOrderByIdDesc(status));
    }
}
