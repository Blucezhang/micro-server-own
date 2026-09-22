-- Merchant settlement ledger. Payment callbacks create receivables; refunds reverse unsettled receivables.
CREATE TABLE IF NOT EXISTS stl_merchant_receivable (
  id BIGINT NOT NULL AUTO_INCREMENT, order_no VARCHAR(64) NOT NULL, merchant_id BIGINT NOT NULL,
  gross_amount DECIMAL(19,2) NOT NULL, merchant_amount DECIMAL(19,2) NOT NULL, platform_amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(24) NOT NULL, settled_batch_no VARCHAR(64) NULL, created_at DATETIME NOT NULL, reversed_at DATETIME NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_stl_receivable_order_merchant (order_no, merchant_id),
  KEY idx_stl_receivable_merchant_status (merchant_id, status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS stl_batch (
  id BIGINT NOT NULL AUTO_INCREMENT, batch_no VARCHAR(64) NOT NULL, period_start DATETIME NOT NULL, period_end DATETIME NOT NULL,
  status VARCHAR(24) NOT NULL, created_at DATETIME NOT NULL, PRIMARY KEY (id), UNIQUE KEY uk_stl_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS stl_withdrawal (
  id BIGINT NOT NULL AUTO_INCREMENT, withdrawal_no VARCHAR(64) NOT NULL, merchant_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL, status VARCHAR(24) NOT NULL, created_at DATETIME NOT NULL, paid_at DATETIME NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_stl_withdrawal_no (withdrawal_no), KEY idx_stl_withdrawal_merchant (merchant_id, status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
