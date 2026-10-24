"""Static migration-layout checks against disposable project fixtures."""

import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


class MigrationLayoutTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="migration-layout-")
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        (self.root / "scripts").mkdir()
        self.migrations = self.root / "database" / "mysql"
        self.migrations.mkdir(parents=True)
        self.script = self.root / "scripts" / "validate-migrations.sh"
        shutil.copyfile(Path(__file__).with_name("validate-migrations.sh"), self.script)
        (self.root / "docker-compose.yml").write_text(
            "services:\n  mysql:\n    volumes:\n"
            "      - ./database/mysql:/docker-entrypoint-initdb.d:ro\n",
            encoding="utf-8",
        )
        (self.migrations / "micro.sql").write_text("-- base schema\n", encoding="utf-8")
        (self.migrations / "02-trade.sql").write_text("-- forward migration\n", encoding="utf-8")
        (self.migrations / "03-reliability.sql").write_text("-- forward migration\n", encoding="utf-8")

    def run_check(self):
        return subprocess.run(["bash", str(self.script)], capture_output=True, text=True, check=False)

    def test_current_directory_mount_and_contiguous_migrations_are_valid(self):
        result = self.run_check()
        self.assertEqual(0, result.returncode, result.stderr)

    def test_missing_directory_mount_is_rejected(self):
        (self.root / "docker-compose.yml").write_text("services: {}\n", encoding="utf-8")
        self.assertNotEqual(0, self.run_check().returncode)

    def test_migration_gap_is_rejected(self):
        (self.migrations / "03-reliability.sql").rename(self.migrations / "04-reliability.sql")
        result = self.run_check()
        self.assertNotEqual(0, result.returncode)
        self.assertIn("expected migration 03", result.stderr)

    def test_missing_base_schema_is_rejected(self):
        (self.migrations / "micro.sql").unlink()
        self.assertNotEqual(0, self.run_check().returncode)

    def test_missing_numbered_migrations_are_rejected(self):
        (self.migrations / "02-trade.sql").unlink()
        (self.migrations / "03-reliability.sql").unlink()
        self.assertNotEqual(0, self.run_check().returncode)


if __name__ == "__main__":
    unittest.main()
