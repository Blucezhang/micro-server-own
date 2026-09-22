-- Coupon holds must expire even if the caller cannot send a compensating release.
-- Apply after 13-buyer-address-default-lock.sql. The new column is nullable for forward compatibility.
ALTER TABLE mkt_user_coupon
  ADD COLUMN reservation_expires_at DATETIME NULL,
  ADD KEY idx_mkt_coupon_reservation_expiry (status, reservation_expires_at);
