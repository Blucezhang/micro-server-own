-- Forward-only expansion; pause order/settlement writes until migrations 36-38 finish.
-- MySQL 5.7 DDL auto-commits. Do not edit already deployed migrations.
ALTER TABLE pay_after_sale_refund
  ADD COLUMN merchant_id BIGINT NULL,
  ADD COLUMN merchant_refund_amount DECIMAL(19,2) NULL,
  ADD COLUMN platform_refund_amount DECIMAL(19,2) NULL,
  ADD COLUMN reconciliation_status VARCHAR(32) NULL,
  ADD KEY idx_after_sale_refund_merchant (order_no, merchant_id, reconciliation_status),
  ADD KEY idx_after_sale_refund_reconcile (reconciliation_status, id);
