package com.own.settlement.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.settlement.repository.RecoveryOperationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettlementRecoveryControllerTest {
    private static final String TOKEN = "isolated-internal-token-at-least-32-bytes";
    private final RecoveryOperationRepository repository = mock(RecoveryOperationRepository.class);
    private final SettlementRecoveryController controller = new SettlementRecoveryController(repository, new InternalServiceGuard(TOKEN, ""));

    @Test
    void missingTokenAndNonSystemActorCannotReadFinancialRecoveryRecords() {
        assertEquals(403, assertThrows(TradeException.class, () -> controller.operations("PENDING", request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class, () -> controller.operations("PENDING", request(TOKEN, "BUYER"))).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void invalidStateDoesNotFallBackToListingEverything() {
        assertEquals(422, assertThrows(TradeException.class, () -> controller.operations("ALL", request(TOKEN, "SYSTEM"))).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void trustedReadIsBoundedToSelectedRecoveryState() {
        controller.operations("RECONCILIATION_REQUIRED", request(TOKEN, "SYSTEM"));
        verify(repository).findTop100ByStatusOrderByIdDesc("RECONCILIATION_REQUIRED");
    }

    private HttpServletRequest request(String token, String actor) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(InternalServiceGuard.HEADER)).thenReturn(token);
        when(request.getHeader("X-Actor-Id")).thenReturn("1"); when(request.getHeader("X-Actor-Type")).thenReturn(actor);
        return request;
    }
}
