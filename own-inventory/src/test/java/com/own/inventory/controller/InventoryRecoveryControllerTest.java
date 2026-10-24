package com.own.inventory.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.repository.InventoryReservationRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryRecoveryControllerTest {
    private static final String TOKEN = "isolated-internal-token-at-least-32-bytes";
    private final InventoryReservationRepository rows = mock(InventoryReservationRepository.class);
    private final InventoryRecoveryController controller = new InventoryRecoveryController(rows, new InternalServiceGuard(TOKEN, ""));
    @Test
    void untrustedRequestsCannotReadOrderReservationFacts() {
        assertEquals(403, assertThrows(TradeException.class, () -> controller.state("ORD-1", request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class, () -> controller.state("ORD-1", request(TOKEN, "BUYER"))).getStatus());
        verifyNoInteractions(rows);
    }
    @Test
    void projectsCurrentSkuOwnershipQuantityAndStateWithoutMutatingInventory() {
        InventoryReservation row = new InventoryReservation("RSV-1", "ORD-1", 101L, 20L, 2, new Date()); row.commit();
        when(rows.findByOrderNoOrderByIdAsc("ORD-1")).thenReturn(List.of(row));
        Object data = controller.state("ORD-1", request(TOKEN, "SYSTEM")).getData();
        assertEquals(List.of(Map.of("orderNo", "ORD-1", "productId", 101L, "merchantId", 20L, "quantity", 2, "status", "COMMITTED")), data);
        verify(rows).findByOrderNoOrderByIdAsc("ORD-1"); verifyNoMoreInteractions(rows);
    }
    private HttpServletRequest request(String token, String actor) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(InternalServiceGuard.HEADER)).thenReturn(token);
        when(request.getHeader("X-Actor-Id")).thenReturn("1"); when(request.getHeader("X-Actor-Type")).thenReturn(actor);
        return request;
    }
}
