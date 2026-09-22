-- Server-side refresh-session ledger. Raw refresh JWTs are never persisted.
CREATE TABLE IF NOT EXISTS auth_refresh_session (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  actor_type VARCHAR(16) NOT NULL,
  expires_at DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL,
  replaced_by VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  revoked_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_refresh_token_id (token_id),
  KEY idx_auth_refresh_user_status (user_id, status),
  KEY idx_auth_refresh_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
