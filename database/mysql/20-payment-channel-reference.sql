-- Reference data for the local channel simulator. No channel credentials are stored in this table.
ALTER TABLE pay_payment
  ADD COLUMN payment_channel VARCHAR(32) NOT NULL DEFAULT 'MOCK_WECHAT' AFTER request_key,
  ADD COLUMN provider_payment_no VARCHAR(96) NOT NULL DEFAULT '' AFTER payment_channel;
