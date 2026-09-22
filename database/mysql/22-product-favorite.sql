CREATE TABLE IF NOT EXISTS prd_buyer_favorite (
  id BIGINT NOT NULL AUTO_INCREMENT,
  buyer_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prd_favorite_buyer_product (buyer_id, product_id),
  KEY idx_prd_favorite_buyer_created (buyer_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
