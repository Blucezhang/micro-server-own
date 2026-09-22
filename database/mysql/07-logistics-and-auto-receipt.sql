-- P2 logistics traces and automatic receipt support; no financial data is modified.
ALTER TABLE ord_sub_order ADD COLUMN shipped_at DATETIME NULL;
CREATE TABLE IF NOT EXISTS ord_logistics_trace (
  id BIGINT NOT NULL AUTO_INCREMENT, sub_order_no VARCHAR(64) NOT NULL,
  trace_status VARCHAR(64) NOT NULL, detail VARCHAR(500) NOT NULL, actor_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL, PRIMARY KEY (id), KEY idx_ord_logistics_trace_sub (sub_order_no, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
