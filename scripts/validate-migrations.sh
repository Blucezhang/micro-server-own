#!/usr/bin/env bash
set -eu

root_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
migration_dir="$root_dir/database/mysql"
compose_file="$root_dir/docker-compose.yml"

if [ ! -f "$migration_dir/micro.sql" ]; then
    echo "missing base schema database/mysql/micro.sql" >&2
    exit 1
fi
if [ ! -f "$compose_file" ] || ! grep -Eq '^[[:space:]]*-[[:space:]]+\./database/mysql:/docker-entrypoint-initdb\.d:ro([[:space:]]|$)' "$compose_file"; then
    echo "missing read-only MySQL migration directory mount in docker-compose.yml" >&2
    exit 1
fi

expected=2
for migration in "$migration_dir"/[0-9][0-9]-*.sql; do
    test -f "$migration" || continue
    filename=$(basename "$migration")
    number=${filename%%-*}
    if [ "$number" -ne "$expected" ]; then
        echo "expected migration $(printf '%02d' "$expected"), found $filename" >&2
        exit 1
    fi
    expected=$((expected + 1))
done

if [ "$expected" -eq 2 ]; then
    echo "no numbered forward migrations found" >&2
    exit 1
fi

echo "validated base micro.sql and migration files 02 through $(printf '%02d' $((expected - 1))) (static layout only)"
