ALTER TABLE auth_login_failure
  ADD KEY idx_auth_login_failure_updated_at (updated_at);
