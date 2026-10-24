-- Forward-only. Pause order/settlement writers for coordinated deployment.
-- Existing rows remain NONE with no invented payment-start timestamp.
ALTER TABLE ord_order
  ADD COLUMN payment_started_at DATETIME NULL,
  ADD COLUMN payment_recovery_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN payment_recovery_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
  ADD KEY idx_order_payment_recovery (status, payment_recovery_status, payment_started_at);

-- No payment FK: independent intent commit must not wait on the outer payment row lock.
CREATE TABLE stl_recovery_operation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  operation_key VARCHAR(100) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  action VARCHAR(16) NOT NULL,
  status VARCHAR(32) NOT NULL,
  attempts INT NOT NULL,
  next_attempt_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  completed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_recovery_operation (operation_key),
  KEY idx_recovery_due (status, next_attempt_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
