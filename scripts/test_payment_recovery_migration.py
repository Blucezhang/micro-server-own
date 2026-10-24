"""Exercise portable column definitions on old rows; MySQL DDL/indexes remain unexecuted."""

import re
import sqlite3
import unittest
from pathlib import Path


SQL = (Path(__file__).resolve().parents[1] / "database/mysql/39-payment-recovery.sql").read_text()


class PaymentRecoveryMigrationTest(unittest.TestCase):
    def setUp(self):
        self.database = sqlite3.connect(":memory:")
        self.addCleanup(self.database.close)
        self.database.execute("CREATE TABLE ord_order (order_no TEXT, status TEXT, payable_amount TEXT)")

    def expand_columns(self):
        definitions = re.findall(r"ADD COLUMN ([^\n]+)", SQL)
        self.assertEqual(3, len(definitions))
        for definition in definitions:
            self.database.execute("ALTER TABLE ord_order ADD COLUMN " + definition.rstrip(","))

    def test_existing_states_amounts_and_unknown_start_time_are_preserved(self):
        before = [("OLD-1", "PENDING_PAYMENT", "10.00"), ("OLD-2", "PAID", "20.00"),
                  ("OLD-3", "REFUNDING", "30.00"), ("OLD-4", "REFUNDED", "40.00")]
        self.database.executemany("INSERT INTO ord_order VALUES (?, ?, ?)", before)
        self.expand_columns()
        self.assertEqual(before, self.database.execute("SELECT order_no, status, payable_amount FROM ord_order").fetchall())
        self.assertEqual([(None, 0, "NONE")] * 4, self.database.execute(
            "SELECT payment_started_at, payment_recovery_attempts, payment_recovery_status FROM ord_order").fetchall())

    def test_new_untouched_rows_have_no_synthetic_payment_intent(self):
        self.expand_columns()
        self.database.execute("INSERT INTO ord_order(order_no, status, payable_amount) VALUES ('NEW-1', 'PENDING_PAYMENT', '5.00')")
        self.assertEqual((None, 0, "NONE"), self.database.execute(
            "SELECT payment_started_at, payment_recovery_attempts, payment_recovery_status FROM ord_order").fetchone())

    def test_journal_is_independent_and_has_one_business_key_and_due_index(self):
        sql = re.sub(r"--[^\n]*", "", SQL)
        self.assertNotRegex(sql, r"\b(UPDATE|INSERT|DELETE)\b")
        self.assertNotRegex(sql, r"\b(FOREIGN KEY|REFERENCES)\b")
        self.assertIn("UNIQUE KEY uk_recovery_operation (operation_key)", sql)
        self.assertIn("KEY idx_recovery_due (status, next_attempt_at, id)", sql)
        self.assertIn("buyer_id BIGINT NOT NULL", sql)
        self.assertIn("amount DECIMAL(19,2) NOT NULL", sql)
        self.assertIn("action VARCHAR(16) NOT NULL", sql)


if __name__ == "__main__":
    unittest.main()
