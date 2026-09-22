-- Serialize cart writes for a buyer so concurrent add operations merge one SKU line.
CREATE TABLE IF NOT EXISTS ord_cart_buyer_guard (
  buyer_id BIGINT NOT NULL,
  PRIMARY KEY (buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
