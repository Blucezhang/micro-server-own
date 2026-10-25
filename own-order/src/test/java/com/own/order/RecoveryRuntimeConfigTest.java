package com.own.order;

import com.own.order.service.OrderPaymentRecoveryScheduler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryRuntimeConfigTest {
    @Test
    void requestsHaveFiniteTimeoutsAndInvalidValuesCannotDisableThem() {
        OrderApplication app = new OrderApplication();
        var client = app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(3000, 10000));
        assertEquals(3000, ReflectionTestUtils.getField(client.getRequestFactory(), "connectTimeout"));
        assertEquals(10000, ReflectionTestUtils.getField(client.getRequestFactory(), "readTimeout"));
        assertThrows(IllegalArgumentException.class, () -> app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(0, 1000)));
        assertThrows(IllegalArgumentException.class, () -> app.restTemplate(new com.own.face.trade.config.TradeHttpProperties(1000, -1)));
    }
    @Test
    void recoveryDoesNotUseTheDefaultSchedulerThread() throws Exception {
        assertEquals("tradeRecoveryScheduler", OrderPaymentRecoveryScheduler.class.getMethod("recover").getAnnotation(Scheduled.class).scheduler());
        assertEquals("order-recovery-", new OrderApplication().recoveryScheduler().getThreadNamePrefix());
    }
    @Test
    void createdRequestsCarryTimeoutsWithoutOpeningANetworkConnection() throws Exception {
        var client = new OrderApplication().restTemplate(new com.own.face.trade.config.TradeHttpProperties(1000, 100));
        var request = client.getRequestFactory().createRequest(URI.create("http://fixture.invalid/pending"), org.springframework.http.HttpMethod.GET);
        HttpURLConnection connection = (HttpURLConnection) ReflectionTestUtils.getField(request, "connection");
        assertNotNull(connection);
        assertEquals(1000, connection.getConnectTimeout()); assertEquals(100, connection.getReadTimeout());
    }

    @Test
    void scheduledJobsActuallyResolveDifferentExecutorsInASpringContext() throws Exception {
        var app = new OrderApplication();
        Probe probe = new Probe();
        try (var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            context.register(SchedulingEnabled.class);
            context.registerBean("taskScheduler", org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler.class, app::backgroundScheduler);
            context.registerBean("tradeRecoveryScheduler", org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler.class, app::recoveryScheduler);
            context.registerBean("probe", Probe.class, () -> probe);
            context.refresh();
            assertTrue(probe.defaultRan.await(3, TimeUnit.SECONDS)); assertTrue(probe.recoveryRan.await(3, TimeUnit.SECONDS));
            assertTrue(probe.defaultThread.startsWith("order-background-"));
            assertTrue(probe.recoveryThread.startsWith("order-recovery-"));
        }
    }
    @org.springframework.context.annotation.Configuration
    @org.springframework.scheduling.annotation.EnableScheduling
    static class SchedulingEnabled { }
    static class Probe {
        final CountDownLatch defaultRan = new CountDownLatch(1);
        final CountDownLatch recoveryRan = new CountDownLatch(1);
        volatile String defaultThread;
        volatile String recoveryThread;
        @Scheduled(fixedDelay = 100000)
        public void ordinary() { defaultThread = Thread.currentThread().getName(); defaultRan.countDown(); }
        @Scheduled(fixedDelay = 100000, scheduler = "tradeRecoveryScheduler")
        public void recovery() { recoveryThread = Thread.currentThread().getName(); recoveryRan.countDown(); }
    }
}
