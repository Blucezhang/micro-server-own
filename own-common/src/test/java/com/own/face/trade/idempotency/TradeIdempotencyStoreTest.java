package com.own.face.trade.idempotency;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TradeIdempotencyStoreTest {
    private final TradeActor buyer = new TradeActor(1L, ActorType.BUYER);

    @Test
    void expiredProcessingRecordCannotBeDeletedAndReplayed() {
        TradeIdempotencyRecordTransactions operations = Mockito.mock(TradeIdempotencyRecordTransactions.class);
        IdempotencyRecord uncertain = new IdempotencyRecord("order", 1L, "BUYER", "/api/v1/orders", "key", "hash",
                new java.util.Date(System.currentTimeMillis() - 1000));
        Mockito.when(operations.lookup("order", buyer, "/api/v1/orders", "key")).thenReturn(uncertain);
        com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> new TradeIdempotencyStore(operations).begin("order", buyer, "/api/v1/orders", "key", "hash", 24));
        Assertions.assertEquals(409, error.getStatus());
        Mockito.verify(operations, Mockito.never()).delete(Mockito.any());
        Mockito.verify(operations, Mockito.never()).create(Mockito.anyString(), Mockito.any(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void completedRecordStillExpiresAfterItsRetentionWindow() {
        TradeIdempotencyRecordTransactions operations = Mockito.mock(TradeIdempotencyRecordTransactions.class);
        IdempotencyRecord completed = new IdempotencyRecord("order", 1L, "BUYER", "/api/v1/orders", "key", "hash",
                new java.util.Date(System.currentTimeMillis() - 1000));
        completed.complete(200, "{}");
        IdempotencyRecord next = new IdempotencyRecord("order", 1L, "BUYER", "/api/v1/orders", "key", "hash",
                new java.util.Date(System.currentTimeMillis() + 1000));
        Mockito.when(operations.lookup("order", buyer, "/api/v1/orders", "key")).thenReturn(completed);
        Mockito.when(operations.create("order", buyer, "/api/v1/orders", "key", "hash", 24)).thenReturn(next);
        Assertions.assertSame(next, new TradeIdempotencyStore(operations).begin("order", buyer, "/api/v1/orders", "key", "hash", 24));
        Mockito.verify(operations).delete(completed.getId());
    }

    @Test
    public void beginsNewRecordThroughTransactionalOperationBean() {
        TradeIdempotencyRecordTransactions operations = Mockito.mock(TradeIdempotencyRecordTransactions.class);
        IdempotencyRecord created = new IdempotencyRecord("order", 1L, "BUYER", "/api/v1/orders", "key", "hash", new java.util.Date(System.currentTimeMillis() + 1000));
        Mockito.when(operations.create("order", buyer, "/api/v1/orders", "key", "hash", 24)).thenReturn(created);
        TradeIdempotencyStore store = new TradeIdempotencyStore(operations);

        Assertions.assertSame(created, store.begin("order", buyer, "/api/v1/orders", "key", "hash", 24));
        Mockito.verify(operations).lookup("order", buyer, "/api/v1/orders", "key");
        Mockito.verify(operations).create("order", buyer, "/api/v1/orders", "key", "hash", 24);
    }

    @Test
    public void uniqueKeyRaceReadsCompletedRecordFromFreshOperation() {
        TradeIdempotencyRecordTransactions operations = Mockito.mock(TradeIdempotencyRecordTransactions.class);
        IdempotencyRecord completed = new IdempotencyRecord("order", 1L, "BUYER", "/api/v1/orders", "key", "hash", new java.util.Date(System.currentTimeMillis() + 1000));
        completed.complete(200, "{}");
        Mockito.when(operations.lookup("order", buyer, "/api/v1/orders", "key")).thenReturn(null, completed);
        Mockito.when(operations.create("order", buyer, "/api/v1/orders", "key", "hash", 24))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        TradeIdempotencyStore store = new TradeIdempotencyStore(operations);

        Assertions.assertSame(completed, store.begin("order", buyer, "/api/v1/orders", "key", "hash", 24));
        Mockito.verify(operations, Mockito.times(2)).lookup("order", buyer, "/api/v1/orders", "key");
    }

    @Test
    public void recordOperationsDeclareIndependentTransactions() throws Exception {
        Method create = TradeIdempotencyRecordTransactions.class.getMethod("create", String.class, TradeActor.class,
                String.class, String.class, String.class, int.class);
        Method complete = TradeIdempotencyRecordTransactions.class.getMethod("complete", Long.class,
                com.own.face.util.Resp.class);
        Assertions.assertEquals(Propagation.REQUIRES_NEW, create.getAnnotation(Transactional.class).propagation());
        Assertions.assertEquals(Propagation.REQUIRES_NEW, complete.getAnnotation(Transactional.class).propagation());
    }
}
