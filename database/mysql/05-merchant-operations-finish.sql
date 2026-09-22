-- Completes the P1 forward migration without rewriting earlier migration files.
ALTER TABLE ord_order ADD COLUMN freight_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00;
