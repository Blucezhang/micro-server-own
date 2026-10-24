-- Run only after 37 has classified every historical row. No default: the obsolete
-- writer must not silently create new unattributed refunds after this cutover.
-- Forward-only: do not drop financial allocation facts to roll application code back.
ALTER TABLE pay_after_sale_refund
  MODIFY COLUMN reconciliation_status VARCHAR(32) NOT NULL;
