#!/usr/bin/env bash
set -euo pipefail

archive=${1:?usage: verify-mysql-backup.sh /absolute/path/to/backup.sql.gz}
case "$archive" in
  /*) ;;
  *) echo "backup archive path must be absolute" >&2; exit 2;;
esac
manifest="$archive.sha256"
test -f "$archive" || { echo "backup archive was not found: $archive" >&2; exit 1; }
test -f "$manifest" || { echo "backup checksum was not found: $manifest" >&2; exit 1; }

gzip -t "$archive"
expected=$(awk 'NR == 1 { print $1; next } { exit 1 }' "$manifest")
case "$expected" in
  *[!0123456789abcdefABCDEF]*|'') echo "backup checksum has an invalid format" >&2; exit 1;;
esac
[ "${#expected}" -eq 64 ] || { echo "backup checksum has an invalid length" >&2; exit 1; }
if command -v sha256sum >/dev/null 2>&1; then
  actual=$(sha256sum "$archive" | awk '{print $1}')
else
  actual=$(shasum -a 256 "$archive" | awk '{print $1}')
fi
[ "$actual" = "$expected" ] || { echo "backup SHA-256 does not match manifest" >&2; exit 1; }
echo "backup archive passed gzip and SHA-256 verification"
