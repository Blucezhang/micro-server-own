-- Records access metadata only; it must never contain recipient names, mobiles or address text.
CREATE TABLE IF NOT EXISTS ord_sensitive_access_audit (
  id BIGINT NOT NULL AUTO_INCREMENT, actor_id BIGINT NOT NULL, actor_type VARCHAR(16) NOT NULL,
  action VARCHAR(64) NOT NULL, resource_type VARCHAR(32) NOT NULL, resource_id VARCHAR(64) NOT NULL,
  correlation_id VARCHAR(100) NULL, created_at DATETIME NOT NULL, PRIMARY KEY (id),
  KEY idx_ord_access_audit_resource (resource_type, resource_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
