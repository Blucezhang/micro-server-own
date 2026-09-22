#!/usr/bin/env bash
set -eu

root_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
migration_dir="$root_dir/database/mysql"
compose_file="$root_dir/docker-compose.yml"

test -f "$migration_dir/micro.sql"
test -f "$compose_file"
grep -Fq './database/mysql/micro.sql:/docker-entrypoint-initdb.d/01-micro.sql:ro' "$compose_file"

expected=2
for migration in "$migration_dir"/[0-9][0-9]-*.sql; do
    test -f "$migration" || continue
    filename=$(basename "$migration")
    number=${filename%%-*}
    if [ "$number" -ne "$expected" ]; then
        echo "expected migration $(printf '%02d' "$expected"), found $filename" >&2
        exit 1
    fi
    target="./database/mysql/$filename:/docker-entrypoint-initdb.d/$(printf '%02d' "$expected")-${filename#*-}:ro"
    if ! grep -Fq -- "$target" "$compose_file"; then
        echo "missing Compose mount for $filename" >&2
        exit 1
    fi
    expected=$((expected + 1))
done

echo "validated migrations 01 through $(printf '%02d' $((expected - 1)))"
