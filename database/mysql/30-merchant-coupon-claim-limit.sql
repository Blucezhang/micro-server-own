ALTER TABLE mkt_user_coupon
  ADD COLUMN source_key VARCHAR(128) NULL,
  ADD UNIQUE KEY uk_mkt_coupon_buyer_source (buyer_id, source_key);
