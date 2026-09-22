-- P2 merchant audit reminder state; no automatic approval or rejection.
ALTER TABLE ord_after_sale ADD COLUMN last_reminder_at DATETIME NULL;
CREATE INDEX idx_ord_after_sale_reminder ON ord_after_sale (status, created_at, last_reminder_at);
