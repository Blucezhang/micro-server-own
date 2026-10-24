"""Execute the portable backfill on SQLite fixtures; never claim MySQL DDL execution."""

import re
import sqlite3
import unittest
from pathlib import Path


MIGRATIONS = Path(__file__).resolve().parents[1] / "database" / "mysql"


class RefundAllocationMigrationTest(unittest.TestCase):
    def setUp(self):
        self.database = sqlite3.connect(":memory:")
        self.addCleanup(self.database.close)
        self.database.executescript("""
            CREATE TABLE pay_after_sale_refund (
                id INTEGER PRIMARY KEY, after_sale_no TEXT UNIQUE, order_no TEXT,
                amount TEXT, status TEXT, merchant_id INTEGER,
                merchant_refund_amount TEXT, platform_refund_amount TEXT,
                reconciliation_status TEXT
            );
            CREATE TABLE stl_merchant_receivable (
                order_no TEXT, merchant_id INTEGER, gross_amount TEXT,
                merchant_amount TEXT, platform_amount TEXT, status TEXT
            );
            CREATE TABLE stl_withdrawal (merchant_id INTEGER, amount TEXT, status TEXT);
        """)

    def backfill(self):
        self.database.executescript((MIGRATIONS / "37-after-sale-refund-reconciliation-backfill.sql").read_text())

    def add_legacy(self, identifier, order_no, amount="4.00", status="SUCCEEDED"):
        self.database.execute(
            "INSERT INTO pay_after_sale_refund(id, after_sale_no, order_no, amount, status) VALUES (?, ?, ?, ?, ?)",
            (identifier, "AS-" + str(identifier), order_no, amount, status),
        )

    def test_empty_history_remains_empty(self):
        self.backfill()
        self.assertEqual([], self.database.execute("SELECT * FROM pay_after_sale_refund").fetchall())

    def test_both_single_and_multi_merchant_history_remain_unattributed(self):
        for identifier, order_no in [(1, "SINGLE"), (2, "MULTI"), (3, "NO-RECEIVABLE")]:
            self.add_legacy(identifier, order_no)
        self.database.executemany(
            "INSERT INTO stl_merchant_receivable VALUES (?, ?, '10.00', '8.00', '2.00', 'SETTLED')",
            [("SINGLE", 20), ("MULTI", 20), ("MULTI", 30)],
        )
        self.backfill()
        rows = self.database.execute("""
            SELECT merchant_id, merchant_refund_amount, platform_refund_amount, reconciliation_status
            FROM pay_after_sale_refund ORDER BY id
        """).fetchall()
        self.assertEqual([(None, None, None, "PENDING_RECONCILIATION")] * 3, rows)

    def test_preserves_refund_amounts_statuses_original_splits_and_withdrawals(self):
        self.add_legacy(1, "ORD-1", "0.01")
        self.add_legacy(2, "ORD-1", "0.02", "FAILED")
        self.database.execute("INSERT INTO stl_merchant_receivable VALUES ('ORD-1', 20, '0.03', '0.02', '0.01', 'SETTLED')")
        self.database.execute("INSERT INTO stl_withdrawal VALUES (20, '100.00', 'PAID')")
        refund_before = self.database.execute("SELECT id, after_sale_no, order_no, amount, status FROM pay_after_sale_refund").fetchall()
        original_before = self.database.execute("SELECT * FROM stl_merchant_receivable").fetchall()
        withdrawal_before = self.database.execute("SELECT * FROM stl_withdrawal").fetchall()
        self.backfill()
        self.assertEqual(refund_before, self.database.execute("SELECT id, after_sale_no, order_no, amount, status FROM pay_after_sale_refund").fetchall())
        self.assertEqual(original_before, self.database.execute("SELECT * FROM stl_merchant_receivable").fetchall())
        self.assertEqual(withdrawal_before, self.database.execute("SELECT * FROM stl_withdrawal").fetchall())

    def test_rerun_does_not_overwrite_an_existing_applied_allocation(self):
        self.add_legacy(1, "ORD-1")
        self.database.execute("""
            INSERT INTO pay_after_sale_refund VALUES
                (2, 'AS-2', 'ORD-2', '10.00', 'SUCCEEDED', 20, '8.00', '2.00', 'APPLIED')
        """)
        self.backfill()
        first = self.database.execute("SELECT * FROM pay_after_sale_refund ORDER BY id").fetchall()
        self.backfill()
        self.assertEqual(first, self.database.execute("SELECT * FROM pay_after_sale_refund ORDER BY id").fetchall())
        self.assertEqual((20, "8.00", "2.00", "APPLIED"), first[1][5:])

    def test_schema_backfill_and_constraint_are_separate_and_have_no_guessed_defaults(self):
        schema = self.sql("36-after-sale-refund-allocation-schema.sql")
        backfill = self.sql("37-after-sale-refund-reconciliation-backfill.sql")
        required = self.sql("38-after-sale-refund-allocation-required.sql")
        self.assertNotRegex(schema + required, r"\b(UPDATE|INSERT|DELETE)\b")
        self.assertNotRegex(backfill, r"\b(ALTER|CREATE|DROP|JOIN)\b")
        self.assertRegex(schema, r"ADD COLUMN merchant_id BIGINT NULL")
        self.assertRegex(schema, r"ADD COLUMN merchant_refund_amount DECIMAL\(19,2\) NULL")
        self.assertRegex(schema, r"ADD COLUMN platform_refund_amount DECIMAL\(19,2\) NULL")
        self.assertRegex(required, r"MODIFY COLUMN reconciliation_status VARCHAR\(32\) NOT NULL")
        self.assertNotRegex(schema + required, r"\bDEFAULT\b")
        self.assertNotRegex(backfill, r"SET\s+merchant_id")

    def sql(self, name):
        return re.sub(r"--[^\n]*", "", (MIGRATIONS / name).read_text())


if __name__ == "__main__":
    unittest.main()
