-- Links after-sale transitions to their own immutable Outbox timeline.
ALTER TABLE ord_event ADD COLUMN after_sale_no VARCHAR(64) NULL;
CREATE INDEX idx_ord_event_after_sale ON ord_event (after_sale_no, id);
