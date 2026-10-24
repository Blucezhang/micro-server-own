"""Contract checks for the disposable API acceptance scenario orchestration."""

import unittest
import json
import runpy
import tempfile
from pathlib import Path
from unittest.mock import patch

from marketplace_e2e_finance import disposable_mysql, settle_and_withdraw
from marketplace_e2e_evidence import redact, save_evidence
from marketplace_e2e_writes import verify_catalog_operations


class FakeApi:
    def __init__(self):
        self.calls = []
        self.evidence = []

    def data(self, method, path, token=None, body=None, key=None):
        self.calls.append((method, path, token, body, key))
        if method == "PUT" and "/merchant/products/" in path:
            return {"id": 101}
        if method == "GET" and "/price-audits" in path:
            return {"items": [{"id": 1}]}
        if method == "GET" and path == "/product/api/v1/products/101":
            return {"name": "Updated acceptance product abc", "promotionPrice": "18.90"}
        if method == "GET" and "/inventory/api/v1/stocks/" in path:
            return {"availableQuantity": 7, "reservedQuantity": 0, "soldQuantity": 0}
        if method == "GET" and path == "/order/api/v1/merchant/freight-rule":
            return {"fixedAmount": "2.00"}
        return {}

    def request(self, method, path, token=None, body=None, expected=(200,), headers=None):
        self.calls.append((method, path, token, body, headers))
        if expected != (409,):
            raise AssertionError("the changed-payload request must expect a conflict")


class AcceptanceWriteFlowTest(unittest.TestCase):
    def test_api_data_rejects_failure_inside_http_success_envelope(self):
        api_module = runpy.run_path(str(Path(__file__).with_name("marketplace-e2e.py")))
        api = api_module["MarketplaceApi"]("http://127.0.0.1:1")
        with patch.object(api, "request", return_value={"status": 409, "data": {"id": 1}}):
            with self.assertRaisesRegex(api_module["AcceptanceFailure"], "envelope status 409"):
                api.data("POST", "/synthetic")
        with patch.object(api, "request", return_value={"data": {"id": 1}}):
            with self.assertRaisesRegex(api_module["AcceptanceFailure"], "envelope status None"):
                api.data("POST", "/synthetic")
        with patch.object(api, "request", return_value={"status": 201, "data": {"id": 1}}):
            self.assertEqual({"id": 1}, api.data("POST", "/synthetic"))

    def test_edit_and_stock_replays_keep_the_same_key(self):
        api = FakeApi()
        actors = {"merchant_id": 7, "merchant_token": "synthetic-test-token"}
        verify_catalog_operations(api, actors, 101, "abc", lambda data, key, _: data[key], AssertionError)
        edits = [call for call in api.calls if call[0] == "PUT" and call[1] == "/product/api/v1/merchant/products/101"]
        adjustments = [call for call in api.calls if call[0] == "POST" and call[1].endswith("/adjustments")]
        self.assertEqual(3, len(edits))
        self.assertEqual(edits[0][4], edits[1][4])
        self.assertEqual(edits[0][4], edits[2][4]["Idempotency-Key"])
        self.assertEqual(2, len(adjustments))
        self.assertEqual(adjustments[0][4], adjustments[1][4])

    def test_mysql_fixture_rejects_non_disposable_project(self):
        with patch.dict("os.environ", {"E2E_COMPOSE_PROJECT": "shared-project"}):
            with self.assertRaisesRegex(AssertionError, "disposable E2E Compose"):
                disposable_mysql(FakeApi(), "SELECT 1", AssertionError)

    def test_evidence_redacts_nested_credentials_and_contact_details(self):
        source = {"password": "secret", "accessToken": "jwt", "data": [
            {"mobile": "15550000001", "email": "buyer@example.test", "amount": "100.00"}]}
        self.assertEqual({"password": "[REDACTED]", "accessToken": "[REDACTED]", "data": [
            {"mobile": "[REDACTED]", "email": "[REDACTED]", "amount": "100.00"}]}, redact(source))

    def test_evidence_file_is_new_and_never_overwrites(self):
        with tempfile.TemporaryDirectory(prefix="marketplace-e2e-evidence-") as directory:
            path = Path(directory) / "result.json"
            with patch.dict("os.environ", {"E2E_RESULTS_FILE": str(path)}):
                save_evidence([{"httpStatus": 200}])
                self.assertEqual([{"httpStatus": 200}], json.loads(path.read_text(encoding="utf-8")))
                with self.assertRaises(FileExistsError):
                    save_evidence([{"httpStatus": 500}])

    def test_settlement_flow_checks_reserved_and_paid_balance(self):
        class FinanceApi:
            def __init__(self):
                self.balances = iter(["157.60", "57.60", "57.60"])

            def data(self, method, path, token=None, body=None, key=None):
                if path.endswith("/balance"):
                    return {"availableAmount": next(self.balances)}
                if path.endswith("/batches/run"):
                    return {"batchNo": "STL-1"}
                if path.endswith("/withdrawals"):
                    return {"withdrawalNo": "WTH-1", "status": "REQUESTED"}
                if path.endswith("/simulate-paid"):
                    return {"status": "PAID"}
                raise AssertionError("unexpected finance API call: " + path)

        api = FinanceApi()
        actors = {"merchant_id": 7, "merchant_token": "merchant", "system_token": "system"}
        write = lambda client, method, path, token, body=None, label=None: client.data(method, path, token, body)
        require_field = lambda data, field, context: data[field]
        with patch("marketplace_e2e_finance.disposable_mysql", side_effect=["PENDING", "", "SETTLED", "PAID"]) as mysql:
            settle_and_withdraw(api, actors, "abc", "ORD-1", write, require_field, AssertionError)
        self.assertEqual(4, mysql.call_count)


if __name__ == "__main__":
    unittest.main()
