package com.own.face.trade.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeActor;
import com.own.face.util.Resp;
import java.util.Date;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Transactional repository operations, invoked from a separate bean proxy. */
@Component
public class TradeIdempotencyRecordTransactions {
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public TradeIdempotencyRecordTransactions(IdempotencyRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository; this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public IdempotencyRecord lookup(String serviceName, TradeActor actor, String path, String key) {
        return repository.findByServiceNameAndActorIdAndActorTypeAndRequestPathAndIdempotencyKey(
                serviceName, actor.getId(), actor.getType().name(), path, key);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord create(String serviceName, TradeActor actor, String path, String key,
                                    String hash, int retentionHours) {
        return repository.saveAndFlush(new IdempotencyRecord(serviceName, actor.getId(), actor.getType().name(), path,
                key, hash, new Date(System.currentTimeMillis() + retentionHours * 3600L * 1000L)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long id, Resp response) throws Exception {
        IdempotencyRecord record = repository.findOne(id);
        if (record == null) throw new IllegalStateException("idempotency record disappeared before completion");
        record.complete(response.getStatus(), objectMapper.writeValueAsString(response));
        repository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) { repository.delete(id); }
}
