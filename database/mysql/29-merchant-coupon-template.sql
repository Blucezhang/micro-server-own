CREATE TABLE IF NOT EXISTS mkt_merchant_coupon_template (
  id BIGINT NOT NULL AUTO_INCREMENT,
  merchant_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  total_quantity INT NOT NULL,
  minimum_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  claim_starts_at DATETIME NULL,
  claim_ends_at DATETIME NULL,
  expires_at DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_mkt_merchant_template_merchant (merchant_id, status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
