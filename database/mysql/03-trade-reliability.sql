-- P0 reliability additions. Apply after 02-trade-schema.sql; never rewrite an existing migration.

CREATE TABLE IF NOT EXISTS trade_idempotency (
  id BIGINT NOT NULL AUTO_INCREMENT,
  service_name VARCHAR(64) NOT NULL,
  actor_id BIGINT NOT NULL,
  actor_type VARCHAR(16) NOT NULL,
  request_path VARCHAR(255) NOT NULL,
  idempotency_key VARCHAR(100) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL,
  response_status INT NULL,
  response_body MEDIUMTEXT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  completed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_idempotency_scope (service_name, actor_id, actor_type, request_path, idempotency_key),
  KEY idx_trade_idempotency_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE ord_event
  ADD COLUMN delivery_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  ADD COLUMN delivery_target VARCHAR(64) NOT NULL DEFAULT 'DISABLED',
  ADD COLUMN delivery_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN next_attempt_at DATETIME NULL,
  ADD COLUMN last_delivery_error VARCHAR(500) NULL,
  ADD COLUMN delivered_at DATETIME NULL;

CREATE INDEX idx_ord_event_delivery ON ord_event (delivery_status, next_attempt_at, id);
CREATE INDEX idx_ord_order_timeout ON ord_order (status, created_at);
