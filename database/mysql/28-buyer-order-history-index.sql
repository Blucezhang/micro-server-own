ALTER TABLE ord_order
  ADD KEY idx_ord_order_buyer_created (buyer_id, created_at, id);
