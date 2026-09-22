package com.own.face.trade.idempotency;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "trade_idempotency", uniqueConstraints = @UniqueConstraint(columnNames = {
        "service_name", "actor_id", "actor_type", "request_path", "idempotency_key"}))
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "service_name", nullable = false) private String serviceName;
    @Column(name = "actor_id", nullable = false) private Long actorId;
    @Column(name = "actor_type", nullable = false) private String actorType;
    @Column(name = "request_path", nullable = false) private String requestPath;
    @Column(name = "idempotency_key", nullable = false) private String idempotencyKey;
    @Column(name = "request_hash", nullable = false, length = 64) private String requestHash;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "response_status") private Integer responseStatus;
    @Column(name = "response_body", columnDefinition = "MEDIUMTEXT") private String responseBody;
    @Column(name = "expires_at", nullable = false) private Date expiresAt;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "completed_at") private Date completedAt;

    protected IdempotencyRecord() { }
    public IdempotencyRecord(String serviceName, Long actorId, String actorType, String requestPath,
                             String idempotencyKey, String requestHash, Date expiresAt) {
        this.serviceName = serviceName; this.actorId = actorId; this.actorType = actorType;
        this.requestPath = requestPath; this.idempotencyKey = idempotencyKey; this.requestHash = requestHash;
        this.status = "PROCESSING"; this.expiresAt = expiresAt; this.createdAt = new Date();
    }
    public void complete(int responseStatus, String responseBody) {
        this.status = "COMPLETED"; this.responseStatus = responseStatus; this.responseBody = responseBody;
        this.completedAt = new Date();
    }
    public Long getId() { return id; }
    public String getRequestHash() { return requestHash; }
    public String getStatus() { return status; }
    public Integer getResponseStatus() { return responseStatus; }
    public String getResponseBody() { return responseBody; }
    public Date getExpiresAt() { return expiresAt; }
}
