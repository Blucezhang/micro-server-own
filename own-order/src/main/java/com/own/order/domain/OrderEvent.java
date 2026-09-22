package com.own.order.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ord_event")
public class OrderEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "sub_order_no", length = 64) private String subOrderNo;
    @Column(name = "after_sale_no", length = 64) private String afterSaleNo;
    @Column(name = "event_type", nullable = false, length = 64) private String eventType;
    @Column(name = "actor_id", nullable = false) private Long actorId;
    @Column(name = "actor_type", nullable = false, length = 16) private String actorType;
    @Column(nullable = false, length = 500) private String payload;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "delivery_status", nullable = false, length = 16) private String deliveryStatus;
    @Column(name = "delivery_target", nullable = false, length = 64) private String deliveryTarget;
    @Column(name = "delivery_attempts", nullable = false) private Integer deliveryAttempts;
    @Column(name = "next_attempt_at") private Date nextAttemptAt;
    @Column(name = "last_delivery_error", length = 500) private String lastDeliveryError;
    @Column(name = "delivered_at") private Date deliveredAt;

    protected OrderEvent() { }
    public OrderEvent(String orderNo, String subOrderNo, String eventType, Long actorId, String actorType, String payload) {
        this(orderNo, subOrderNo, null, eventType, actorId, actorType, payload);
    }
    public OrderEvent(String orderNo, String subOrderNo, String afterSaleNo, String eventType, Long actorId, String actorType, String payload) {
        this.orderNo = orderNo; this.subOrderNo = subOrderNo; this.eventType = eventType;
        this.afterSaleNo = afterSaleNo;
        this.actorId = actorId; this.actorType = actorType; this.payload = payload; this.createdAt = new Date();
        this.deliveryStatus = "PENDING"; this.deliveryTarget = "DISABLED"; this.deliveryAttempts = 0;
        this.nextAttemptAt = this.createdAt;
    }
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public String getSubOrderNo() { return subOrderNo; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public String getEventType() { return eventType; }
    public Long getActorId() { return actorId; }
    public String getActorType() { return actorType; }
    public String getPayload() { return payload; }
    public Date getCreatedAt() { return createdAt; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getDeliveryTarget() { return deliveryTarget; }
    public Integer getDeliveryAttempts() { return deliveryAttempts; }
    public Date getNextAttemptAt() { return nextAttemptAt; }
    public String getLastDeliveryError() { return lastDeliveryError; }
    public Date getDeliveredAt() { return deliveredAt; }
    public void recordDeliveryFailure(String error, int delaySeconds) {
        this.deliveryStatus = "PENDING"; this.deliveryAttempts++;
        this.lastDeliveryError = error == null ? null : error.substring(0, Math.min(error.length(), 500));
        this.nextAttemptAt = new Date(System.currentTimeMillis() + delaySeconds * 1000L);
    }
    public void markDeliveryFailed(String error) {
        this.deliveryStatus = "FAILED"; this.deliveryAttempts++;
        this.lastDeliveryError = error == null ? null : error.substring(0, Math.min(error.length(), 500));
        this.nextAttemptAt = null;
    }
    public void markDelivered() { this.deliveryStatus = "DELIVERED"; this.deliveredAt = new Date(); this.lastDeliveryError = null; }
    public void setDeliveryTarget(String target) { this.deliveryTarget = target == null ? "UNKNOWN" : target.substring(0, Math.min(target.length(), 64)); }
}
