CREATE TABLE IF NOT EXISTS auth_login_failure (
  id BIGINT NOT NULL AUTO_INCREMENT,
  login_name VARCHAR(128) NOT NULL,
  failure_count INT NOT NULL,
  locked_until DATETIME NULL,
  last_failure_at DATETIME NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_login_failure_name (login_name),
  KEY idx_auth_login_failure_locked_until (locked_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
