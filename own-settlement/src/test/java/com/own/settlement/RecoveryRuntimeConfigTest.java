package com.own.settlement;

import com.own.settlement.service.SettlementRecoveryScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryRuntimeConfigTest {
    @Test
    void settlementAllowsBoundedDownstreamChainTimeAndRejectsInfiniteTimeouts() {
        SettlementApplication app = new SettlementApplication();
        var client = app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(3000, 30000));
        assertEquals(3000, ReflectionTestUtils.getField(client.getRequestFactory(), "connectTimeout"));
        assertEquals(30000, ReflectionTestUtils.getField(client.getRequestFactory(), "readTimeout"));
        assertThrows(IllegalArgumentException.class, () -> app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(1000, 0)));
        assertThrows(IllegalArgumentException.class, () -> app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(1000, 60001)));
    }
    @Test
    void recoveryHasADedicatedScheduler() throws Exception {
        assertEquals("tradeRecoveryScheduler", SettlementRecoveryScheduler.class.getMethod("recover").getAnnotation(Scheduled.class).scheduler());
        assertEquals("settlement-recovery-", new SettlementApplication().recoveryScheduler().getThreadNamePrefix());
    }
}
