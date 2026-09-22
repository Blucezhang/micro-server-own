#!/usr/bin/env bash
set -euo pipefail

: "${MYSQL_HOST:?set MYSQL_HOST}"
: "${MYSQL_PORT:=3306}"
: "${MYSQL_USER:?set MYSQL_USER}"
: "${MYSQL_PASSWORD:?set MYSQL_PASSWORD}"
: "${MYSQL_DATABASE:?set MYSQL_DATABASE}"
: "${BACKUP_DIR:?set BACKUP_DIR to a directory outside the repository}"
case "$MYSQL_DATABASE" in
  *[!A-Za-z0-9_]*|'') echo "MYSQL_DATABASE must contain only letters, digits and underscores" >&2; exit 2;;
esac
case "$BACKUP_DIR" in
  /*) ;;
  *) echo "BACKUP_DIR must be an absolute path outside the repository" >&2; exit 2;;
esac

command -v mysqldump >/dev/null 2>&1 || { echo "mysqldump is required" >&2; exit 1; }
command -v gzip >/dev/null 2>&1 || { echo "gzip is required" >&2; exit 1; }

umask 077
mkdir -p "$BACKUP_DIR"
timestamp=$(date -u +%Y%m%dT%H%M%SZ)
archive="$BACKUP_DIR/${MYSQL_DATABASE}-${timestamp}.sql.gz"
manifest="$archive.sha256"

MYSQL_PWD="$MYSQL_PASSWORD" mysqldump \
  --host="$MYSQL_HOST" --port="$MYSQL_PORT" --user="$MYSQL_USER" \
  --single-transaction --routines --events --triggers --databases "$MYSQL_DATABASE" \
  | gzip -c > "$archive"

if command -v sha256sum >/dev/null 2>&1; then
  sha256sum "$archive" > "$manifest"
else
  shasum -a 256 "$archive" > "$manifest"
fi

echo "created $archive"
echo "created $manifest"
