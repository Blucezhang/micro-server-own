-- Serialize default-address changes per buyer. Apply after 12-sensitive-access-audit-retention.sql.
CREATE TABLE IF NOT EXISTS buyer_address_guard (
  buyer_id BIGINT NOT NULL,
  PRIMARY KEY (buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
