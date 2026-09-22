-- Transaction-domain schema.  This file is forward-only and safe for a new Compose volume.
-- Existing environments must apply it explicitly; do not edit it after deployment.

CREATE TABLE IF NOT EXISTS inv_stock (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  available_quantity INT NOT NULL,
  reserved_quantity INT NOT NULL,
  sold_quantity INT NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inv_stock_product_merchant (product_id, merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS inv_reservation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  expires_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inv_reservation_no (reservation_no),
  UNIQUE KEY uk_inv_reservation_order_product (order_no, product_id),
  KEY idx_inv_reservation_order_status (order_no, status),
  KEY idx_inv_reservation_expiry (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS mkt_coupon_stock (
  id BIGINT NOT NULL AUTO_INCREMENT,
  scope_key VARCHAR(100) NOT NULL,
  total_quantity INT NOT NULL,
  available_quantity INT NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mkt_coupon_scope (scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS mkt_user_coupon (
  id BIGINT NOT NULL AUTO_INCREMENT,
  coupon_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL,
  coupon_type VARCHAR(16) NOT NULL,
  legacy_ticket_id BIGINT NOT NULL,
  merchant_id BIGINT NULL,
  minimum_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  expires_at DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL,
  reserved_order_no VARCHAR(64) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mkt_coupon_no (coupon_no),
  KEY idx_mkt_coupon_buyer (buyer_id, status),
  KEY idx_mkt_coupon_order (reserved_order_no, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ord_cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  buyer_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  unit_price DECIMAL(19,2) NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_ord_cart_buyer (buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ord_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL,
  total_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  payable_amount DECIMAL(19,2) NOT NULL,
  shipping_address VARCHAR(500) NOT NULL,
  request_key VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ord_order_no (order_no),
  UNIQUE KEY uk_ord_order_request_key (request_key),
  KEY idx_ord_order_buyer (buyer_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ord_sub_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sub_order_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  merchant_id BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL,
  total_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  payable_amount DECIMAL(19,2) NOT NULL,
  logistics_company VARCHAR(100) NULL,
  tracking_no VARCHAR(100) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ord_sub_order_no (sub_order_no),
  KEY idx_ord_sub_order_parent (order_no),
  KEY idx_ord_sub_order_merchant (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ord_order_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  sub_order_no VARCHAR(64) NOT NULL,
  product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  unit_price DECIMAL(19,2) NOT NULL,
  quantity INT NOT NULL,
  line_total DECIMAL(19,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_ord_item_order (order_no),
  KEY idx_ord_item_sub_order (sub_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS ord_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  sub_order_no VARCHAR(64) NULL,
  event_type VARCHAR(64) NOT NULL,
  actor_id BIGINT NOT NULL,
  actor_type VARCHAR(16) NOT NULL,
  payload VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_ord_event_order (order_no, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS pay_payment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payment_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  request_key VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pay_payment_no (payment_no),
  UNIQUE KEY uk_pay_payment_order (order_no),
  UNIQUE KEY uk_pay_payment_request_key (request_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS pay_refund (
  id BIGINT NOT NULL AUTO_INCREMENT,
  refund_no VARCHAR(64) NOT NULL,
  payment_no VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pay_refund_no (refund_no),
  UNIQUE KEY uk_pay_refund_payment (payment_no),
  UNIQUE KEY uk_pay_refund_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
