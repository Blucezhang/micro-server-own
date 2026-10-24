"""Redact sensitive fields from disposable API acceptance transcripts."""

import json
import os


def redact(value):
    if isinstance(value, dict):
        result = {}
        for key, item in value.items():
            lower = str(key).lower()
            result[key] = "[REDACTED]" if any(secret in lower for secret in
                ("password", "token", "secret", "authorization", "mobile", "phone", "email")) else redact(item)
        return result
    if isinstance(value, list):
        return [redact(item) for item in value]
    return value


def save_evidence(records):
    path = os.environ.get("E2E_RESULTS_FILE")
    if not path:
        return
    with open(path, "x", encoding="utf-8") as output:
        json.dump(records, output, ensure_ascii=False, indent=2)
        output.write("\n")
    print("Acceptance evidence: " + path)
