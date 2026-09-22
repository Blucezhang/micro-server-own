-- P0 Saga messaging foundation. Forward-only; no existing table is changed.
CREATE TABLE IF NOT EXISTS trade_message_inbox (
  id BIGINT NOT NULL AUTO_INCREMENT,
  consumer_name VARCHAR(64) NOT NULL,
  message_id VARCHAR(128) NOT NULL,
  payload_hash VARCHAR(64) NOT NULL,
  processed_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_message_inbox_consumer_message (consumer_name, message_id),
  KEY idx_trade_message_inbox_processed (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
