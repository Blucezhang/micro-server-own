-- Forward-only. Deploy together with the order cancellation/payment barrier code.
-- Old orders have no invented cancellation intent; pause order writes for cutover.
ALTER TABLE ord_order
  ADD COLUMN cancellation_started_at DATETIME NULL,
  ADD COLUMN cancellation_recovery_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN cancellation_recovery_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
  ADD COLUMN cancellation_reason VARCHAR(16) NULL,
  ADD KEY idx_order_cancellation_recovery (status, cancellation_recovery_status, cancellation_started_at);
