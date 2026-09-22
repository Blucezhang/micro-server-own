-- One immutable published review per buyer and Product/SKU, only after receipt.
CREATE TABLE IF NOT EXISTS prd_product_review (
  id BIGINT NOT NULL AUTO_INCREMENT,
  buyer_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prd_review_buyer_product (buyer_id, product_id),
  KEY idx_prd_review_product_status (product_id, status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
