-- Async checkout Saga state. Existing orders remain untouched and retain their current states.
CREATE TABLE IF NOT EXISTS trade_order_saga (
  id BIGINT NOT NULL AUTO_INCREMENT,
  saga_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  status VARCHAR(24) NOT NULL,
  current_step VARCHAR(32) NOT NULL,
  command_payload MEDIUMTEXT NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  last_error VARCHAR(500) NULL,
  next_attempt_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_order_saga_no (saga_no),
  UNIQUE KEY uk_trade_order_saga_order (order_no),
  KEY idx_trade_order_saga_retry (status, next_attempt_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
