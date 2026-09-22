-- Buyer-provided after-sale evidence references only actor-bound, permanent file objects.
CREATE TABLE IF NOT EXISTS ord_after_sale_evidence (
  id BIGINT NOT NULL AUTO_INCREMENT,
  after_sale_no VARCHAR(64) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ord_after_sale_evidence_file (after_sale_no, stored_name),
  KEY idx_ord_after_sale_evidence_sale (after_sale_no, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
