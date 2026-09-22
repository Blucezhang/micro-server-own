-- System-managed review/reply moderation vocabulary. Existing configured terms remain a deployment-level fallback.
CREATE TABLE IF NOT EXISTS prd_review_prohibited_term (
  id BIGINT NOT NULL AUTO_INCREMENT,
  normalized_term VARCHAR(100) NOT NULL,
  active TINYINT(1) NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_by BIGINT NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prd_review_prohibited_term (normalized_term),
  KEY idx_prd_review_prohibited_term_active_id (active, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
