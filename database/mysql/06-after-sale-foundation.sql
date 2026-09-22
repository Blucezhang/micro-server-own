-- P2 after-sale foundation. Apply after 05-merchant-operations-finish.sql.
CREATE TABLE IF NOT EXISTS ord_after_sale (
  id BIGINT NOT NULL AUTO_INCREMENT, after_sale_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL, sub_order_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL, merchant_id BIGINT NOT NULL, type VARCHAR(24) NOT NULL,
  status VARCHAR(24) NOT NULL, requested_amount DECIMAL(19,2) NOT NULL,
  reason VARCHAR(500) NOT NULL, return_company VARCHAR(100) NULL, return_tracking_no VARCHAR(100) NULL,
  merchant_remark VARCHAR(500) NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_ord_after_sale_no (after_sale_no),
  KEY idx_ord_after_sale_buyer (buyer_id, status, id), KEY idx_ord_after_sale_merchant (merchant_id, status, id),
  KEY idx_ord_after_sale_sub_order (sub_order_no, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS ord_after_sale_item (
  id BIGINT NOT NULL AUTO_INCREMENT, after_sale_no VARCHAR(64) NOT NULL, order_item_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL, quantity INT NOT NULL, refund_amount DECIMAL(19,2) NOT NULL,
  PRIMARY KEY (id), KEY idx_ord_after_sale_item_sale (after_sale_no), KEY idx_ord_after_sale_item_order (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
