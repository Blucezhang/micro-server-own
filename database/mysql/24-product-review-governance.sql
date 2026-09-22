ALTER TABLE prd_product_review
  ADD COLUMN merchant_reply VARCHAR(500) NULL,
  ADD COLUMN replied_at DATETIME NULL,
  ADD COLUMN moderated_at DATETIME NULL;
