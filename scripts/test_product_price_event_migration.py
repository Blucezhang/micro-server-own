"""Checks portable expansion and legacy preservation; does not execute MySQL or Neo4j."""
import sqlite3
import unittest
from pathlib import Path


class ProductPriceEventMigrationTest(unittest.TestCase):
    def setUp(self):
        root = Path(__file__).resolve().parents[1]
        self.sql = (root / "database/mysql/40-product-price-audit-event.sql").read_text()
        self.constraint = (root / "database/neo4j/price-change-event-constraint.cypher").read_text()
        self.database = sqlite3.connect(":memory:")
        self.addCleanup(self.database.close)
        self.database.execute("CREATE TABLE prd_product_price_audit (id INTEGER PRIMARY KEY, product_id INTEGER, original_price_after NUMERIC)")

    def test_history_keeps_null_event_keys_and_prices(self):
        self.database.execute("INSERT INTO prd_product_price_audit VALUES (1,7,10.12)")
        self.database.execute("INSERT INTO prd_product_price_audit VALUES (2,7,12.34)")
        self.database.executescript(self.sql)
        self.assertEqual([(1, 10.12, None), (2, 12.34, None)], self.database.execute(
            "SELECT id,original_price_after,event_key FROM prd_product_price_audit ORDER BY id").fetchall())

    def test_new_event_keys_are_unique_without_restricting_legacy_nulls(self):
        self.database.executescript(self.sql)
        self.database.execute("INSERT INTO prd_product_price_audit (event_key) VALUES ('event-1')")
        with self.assertRaises(sqlite3.IntegrityError):
            self.database.execute("INSERT INTO prd_product_price_audit (event_key) VALUES ('event-1')")
        self.database.execute("INSERT INTO prd_product_price_audit (event_key) VALUES (NULL)")
        self.database.execute("INSERT INTO prd_product_price_audit (event_key) VALUES (NULL)")

    def test_graph_constraint_uses_legacy_compatible_syntax_and_no_data_rewrite(self):
        self.assertIn("CREATE CONSTRAINT ON (e:ProductPriceChangeEvent) ASSERT e.eventKey IS UNIQUE", self.constraint)
        self.assertNotIn("IF NOT EXISTS", self.constraint)
        self.assertNotRegex(self.sql, r"(?im)^\s*(UPDATE|DELETE|DROP)\b")


if __name__ == "__main__":
    unittest.main()
