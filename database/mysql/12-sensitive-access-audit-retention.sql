-- Supports bounded retention for sensitive-access metadata only.
CREATE INDEX idx_ord_access_audit_created ON ord_sensitive_access_audit (created_at);
