package com.own.face.trade.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    IdempotencyRecord findByServiceNameAndActorIdAndActorTypeAndRequestPathAndIdempotencyKey(
            String serviceName, Long actorId, String actorType, String requestPath, String idempotencyKey);
}
