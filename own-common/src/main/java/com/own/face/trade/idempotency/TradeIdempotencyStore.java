package com.own.face.trade.idempotency;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import java.util.Date;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/** Separate bean so Spring applies REQUIRES_NEW to idempotency record mutations. */
@Component
public class TradeIdempotencyStore {
    private final TradeIdempotencyRecordTransactions operations;

    public TradeIdempotencyStore(TradeIdempotencyRecordTransactions operations) {
        this.operations = operations;
    }

    public IdempotencyRecord begin(String serviceName, TradeActor actor, String path, String key,
                                   String hash, int retentionHours) {
        IdempotencyRecord existing = operations.lookup(serviceName, actor, path, key);
        if (existing != null && existing.getExpiresAt().before(new Date())) {
            operations.delete(existing.getId()); existing = null;
        }
        if (existing != null) return existingFor(existing, hash);
        try { return operations.create(serviceName, actor, path, key, hash, retentionHours); }
        catch (DataIntegrityViolationException exception) {
            existing = operations.lookup(serviceName, actor, path, key);
            if (existing == null) throw exception;
            return existingFor(existing, hash);
        }
    }

    public void complete(Long id, Resp response) throws Exception { operations.complete(id, response); }
    public void abandon(Long id) { operations.delete(id); }

    private IdempotencyRecord existingFor(IdempotencyRecord existing, String hash) {
        if (!existing.getRequestHash().equals(hash)) {
            throw TradeException.conflict("Idempotency-Key was reused with a different request");
        }
        if (!"COMPLETED".equals(existing.getStatus())) {
            throw TradeException.conflict("request with this Idempotency-Key is still processing");
        }
        return existing;
    }
}
