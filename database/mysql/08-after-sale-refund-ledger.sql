-- Independent P2 ledgers preserve existing full-order refund constraints.
CREATE TABLE IF NOT EXISTS pay_after_sale_refund (
  id BIGINT NOT NULL AUTO_INCREMENT, refund_no VARCHAR(64) NOT NULL, after_sale_no VARCHAR(64) NOT NULL,
  payment_no VARCHAR(64) NOT NULL, order_no VARCHAR(64) NOT NULL, amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(16) NOT NULL, created_at DATETIME NOT NULL, PRIMARY KEY (id),
  UNIQUE KEY uk_pay_after_sale_refund_sale (after_sale_no), KEY idx_pay_after_sale_refund_payment (payment_no, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS inv_after_sale_refund (
  id BIGINT NOT NULL AUTO_INCREMENT, after_sale_no VARCHAR(64) NOT NULL, product_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL, quantity INT NOT NULL, created_at DATETIME NOT NULL, PRIMARY KEY (id),
  UNIQUE KEY uk_inv_after_sale_refund_item (after_sale_no, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
