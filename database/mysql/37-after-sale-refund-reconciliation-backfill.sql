-- Historical refunds have no authoritative merchant attribution in this service.
-- Never infer one from an order, even if it currently has only one receivable.
-- Preserve the original refund and split facts; unresolved rows do not affect balances.
UPDATE pay_after_sale_refund
SET reconciliation_status = 'PENDING_RECONCILIATION'
WHERE reconciliation_status IS NULL;
