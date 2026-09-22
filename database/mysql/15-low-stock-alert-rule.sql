-- P1 inventory operations extension. Apply after 14-coupon-reservation-expiry.sql.
-- This is schema-only and forward-only. Existing stock has no alert rule until its merchant configures one.
CREATE TABLE IF NOT EXISTS inv_low_stock_alert_rule (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  threshold_quantity INT NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inv_low_stock_alert_rule (product_id, merchant_id),
  KEY idx_inv_low_stock_alert_merchant (merchant_id, enabled, id),
  KEY idx_inv_low_stock_alert_enabled (enabled, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
