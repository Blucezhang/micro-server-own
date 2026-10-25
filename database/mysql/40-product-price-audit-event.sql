ALTER TABLE prd_product_price_audit ADD COLUMN event_key VARCHAR(64) NULL;
CREATE UNIQUE INDEX uk_prd_price_audit_event_key ON prd_product_price_audit (event_key);
