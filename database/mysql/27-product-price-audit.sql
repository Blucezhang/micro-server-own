CREATE TABLE IF NOT EXISTS prd_product_price_audit (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  original_price_before DECIMAL(19,2) NOT NULL,
  original_price_after DECIMAL(19,2) NOT NULL,
  promotion_price_before DECIMAL(19,2) NULL,
  promotion_price_after DECIMAL(19,2) NULL,
  reason VARCHAR(200) NULL,
  changed_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_prd_price_audit_product_id (product_id, id),
  KEY idx_prd_price_audit_merchant_id (merchant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
