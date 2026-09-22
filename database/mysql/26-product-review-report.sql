-- Buyer reports are auditable and distinct from the moderation decision itself.
CREATE TABLE IF NOT EXISTS prd_product_review_report (
  id BIGINT NOT NULL AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  reporter_id BIGINT NOT NULL,
  reason VARCHAR(200) NOT NULL,
  status VARCHAR(16) NOT NULL,
  resolution_note VARCHAR(500) NULL,
  resolved_by BIGINT NULL,
  reported_at DATETIME NOT NULL,
  resolved_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prd_review_reporter_review (review_id, reporter_id),
  KEY idx_prd_review_report_status_id (status, id),
  KEY idx_prd_review_report_review (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
