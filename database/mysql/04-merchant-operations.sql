-- P1 merchant operations. Apply after 03-trade-reliability.sql.
CREATE TABLE IF NOT EXISTS buyer_address (
  id BIGINT NOT NULL AUTO_INCREMENT, buyer_id BIGINT NOT NULL, recipient_name VARCHAR(64) NOT NULL,
  mobile VARCHAR(20) NOT NULL, province VARCHAR(64) NOT NULL, city VARCHAR(64) NOT NULL,
  district VARCHAR(64) NOT NULL, detail VARCHAR(255) NOT NULL, is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY (id),
  KEY idx_buyer_address_buyer (buyer_id, is_default, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS merchant_freight_rule (
  id BIGINT NOT NULL AUTO_INCREMENT, merchant_id BIGINT NOT NULL, fixed_amount DECIMAL(19,2) NOT NULL,
  free_threshold DECIMAL(19,2) NULL, updated_at DATETIME NOT NULL, PRIMARY KEY (id),
  UNIQUE KEY uk_merchant_freight_rule (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS inventory_adjustment (
  id BIGINT NOT NULL AUTO_INCREMENT, product_id BIGINT NOT NULL, merchant_id BIGINT NOT NULL,
  adjustment_type VARCHAR(16) NOT NULL, quantity INT NOT NULL, before_available INT NOT NULL,
  after_available INT NOT NULL, reason VARCHAR(255) NOT NULL, actor_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL, PRIMARY KEY (id),
  KEY idx_inventory_adjustment_product (product_id, merchant_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE ord_order
  ADD COLUMN address_id BIGINT NULL, ADD COLUMN recipient_name VARCHAR(64) NULL,
  ADD COLUMN recipient_mobile VARCHAR(20) NULL, ADD COLUMN recipient_province VARCHAR(64) NULL,
  ADD COLUMN recipient_city VARCHAR(64) NULL, ADD COLUMN recipient_district VARCHAR(64) NULL,
  ADD COLUMN recipient_detail VARCHAR(255) NULL;
ALTER TABLE ord_sub_order ADD COLUMN freight_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00;
