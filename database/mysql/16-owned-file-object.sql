-- Owned file metadata for private business attachments. Apply after 15-low-stock-alert-rule.sql.
-- Legacy /file/picture and /file/Picture endpoints remain backward-compatible and do not populate this table.
CREATE TABLE IF NOT EXISTS file_owned_object (
  id BIGINT NOT NULL AUTO_INCREMENT,
  stored_name VARCHAR(255) NOT NULL,
  owner_id BIGINT NOT NULL,
  owner_type VARCHAR(16) NOT NULL,
  storage_status VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  promoted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_owned_object_name (stored_name),
  KEY idx_file_owned_object_owner (owner_id, owner_type, storage_status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
