"""Portable expansion checks, not a MySQL migration acceptance test."""
import re
import sqlite3
import unittest
from pathlib import Path


class CancellationRecoveryMigrationTest(unittest.TestCase):
    def setUp(self):
        self.sql = (Path(__file__).resolve().parents[1] / "database/mysql/41-order-cancellation-recovery.sql").read_text()
        self.database = sqlite3.connect(":memory:")
        self.addCleanup(self.database.close)
        self.database.execute("CREATE TABLE ord_order (order_no TEXT, status TEXT, payable_amount NUMERIC)")

    def expand(self):
        for column in re.findall(r"ADD COLUMN\s+([^,;]+)", self.sql, re.I):
            self.database.execute("ALTER TABLE ord_order ADD COLUMN " + column.strip())

    def test_existing_orders_keep_business_state_and_have_no_invented_intent(self):
        for state in ("PENDING_PAYMENT", "PAID", "CANCELED", "REFUNDED"):
            self.database.execute("INSERT INTO ord_order VALUES (?, ?, ?)", (state, state, "12.34"))
        self.expand()
        rows = self.database.execute("SELECT status,payable_amount,cancellation_started_at,"
                                     "cancellation_recovery_attempts,cancellation_recovery_status,cancellation_reason FROM ord_order").fetchall()
        self.assertEqual(4, len(rows))
        for state, amount, started, attempts, recovery, reason in rows:
            self.assertIn(state, ("PENDING_PAYMENT", "PAID", "CANCELED", "REFUNDED"))
            self.assertEqual(12.34, amount)
            self.assertEqual((None, 0, "NONE", None), (started, attempts, recovery, reason))

    def test_new_untouched_order_has_no_cancellation_barrier(self):
        self.expand()
        self.database.execute("INSERT INTO ord_order (order_no,status,payable_amount) VALUES ('NEW','PENDING_PAYMENT',1)")
        self.assertEqual((None, 0, "NONE", None), self.database.execute(
            "SELECT cancellation_started_at,cancellation_recovery_attempts,cancellation_recovery_status,cancellation_reason FROM ord_order").fetchone())

    def test_pending_scan_index_and_forward_only_expansion(self):
        self.assertIn("(status, cancellation_recovery_status, cancellation_started_at)", self.sql)
        self.assertNotRegex(self.sql, r"(?im)^\s*(UPDATE|DELETE|DROP)\b")


if __name__ == "__main__":
    unittest.main()
