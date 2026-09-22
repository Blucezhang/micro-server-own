-- Idempotency ledger for same-SKU after-sale exchanges. Apply after 17-after-sale-evidence.sql.
CREATE TABLE IF NOT EXISTS inv_after_sale_exchange (
  id BIGINT NOT NULL AUTO_INCREMENT,
  after_sale_no VARCHAR(64) NOT NULL,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inv_after_sale_exchange (after_sale_no, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
