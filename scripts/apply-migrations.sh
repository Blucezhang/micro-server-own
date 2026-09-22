#!/usr/bin/env bash
# Applies numbered forward MySQL migrations exactly once and records their SHA-256.
# Do not use --baseline-through unless the target database was verified to contain
# every migration through that number.
set -euo pipefail

root_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
migration_dir="$root_dir/database/mysql"
baseline_through=""

if [ "${1:-}" = "--baseline-through" ]; then
    baseline_through="${2:-}"
    case "$baseline_through" in ''|*[!0-9]*) echo "--baseline-through needs a numeric migration number" >&2; exit 2;; esac
    shift 2
fi
if [ "$#" -ne 0 ]; then
    echo "usage: MYSQL_HOST=... MYSQL_USER=... MYSQL_PASSWORD=... MYSQL_DATABASE=... $0 [--baseline-through N]" >&2
    exit 2
fi

: "${MYSQL_HOST:?set MYSQL_HOST}"
: "${MYSQL_USER:?set MYSQL_USER}"
: "${MYSQL_PASSWORD:?set MYSQL_PASSWORD}"
: "${MYSQL_DATABASE:?set MYSQL_DATABASE}"
case "$MYSQL_DATABASE" in
    *[!A-Za-z0-9_]*|'') echo "MYSQL_DATABASE must contain only letters, digits and underscores" >&2; exit 2;;
esac
mysql_port="${MYSQL_PORT:-3306}"
mysql_cmd=(mysql --protocol=TCP -h "$MYSQL_HOST" -P "$mysql_port" -u "$MYSQL_USER" "$MYSQL_DATABASE")

export MYSQL_PWD="$MYSQL_PASSWORD"
"${mysql_cmd[@]}" <<'SQL'
CREATE TABLE IF NOT EXISTS micro_schema_migration (
  version INT NOT NULL,
  filename VARCHAR(255) NOT NULL,
  checksum CHAR(64) NOT NULL,
  applied_at DATETIME NOT NULL,
  PRIMARY KEY (version),
  UNIQUE KEY uk_micro_schema_migration_filename (filename)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SQL

checksum() { shasum -a 256 "$1" | awk '{print $1}'; }
record_baseline() {
    local number="$1" filename="$2" hash="$3"
    "${mysql_cmd[@]}" -N -B -e "SELECT checksum FROM micro_schema_migration WHERE version=${number}" | grep -qx "$hash" || \
        "${mysql_cmd[@]}" -e "INSERT INTO micro_schema_migration(version, filename, checksum, applied_at) VALUES (${number}, '${filename}', '${hash}', NOW())"
}

for migration in "$migration_dir"/[0-9][0-9]-*.sql; do
    [ -f "$migration" ] || continue
    filename=$(basename "$migration")
    number=${filename%%-*}
    number=$((10#$number))
    hash=$(checksum "$migration")
    recorded=$("${mysql_cmd[@]}" -N -B -e "SELECT checksum FROM micro_schema_migration WHERE version=${number}" || true)
    if [ -n "$recorded" ]; then
        if [ "$recorded" != "$hash" ]; then
            echo "checksum mismatch for $filename; do not modify an applied migration" >&2
            exit 1
        fi
        echo "already applied: $filename"
        continue
    fi
    if [ -n "$baseline_through" ] && [ "$number" -le "$baseline_through" ]; then
        record_baseline "$number" "$filename" "$hash"
        echo "baselined: $filename"
        continue
    fi
    echo "applying: $filename"
    "${mysql_cmd[@]}" < "$migration"
    record_baseline "$number" "$filename" "$hash"
done

echo "migration run completed"
