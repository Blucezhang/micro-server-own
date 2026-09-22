#!/bin/sh
set -eu

EUREKA_USER_VALUE=${EUREKA_USER:-own}
: "${EUREKA_PASSWORD:?export EUREKA_PASSWORD before running the smoke test}"

wait_for_url() {
  name=$1
  url=$2
  shift 2
  echo "Waiting for ${name}: ${url}"
  curl --fail --silent --show-error --retry 30 --retry-delay 2 --retry-connrefused "$@" "$url" >/dev/null
}

wait_for_url eureka http://localhost:8002/health --user "${EUREKA_USER_VALUE}:${EUREKA_PASSWORD}"
wait_for_url config http://localhost:8001/health
wait_for_url gateway http://localhost:9632/health

curl --fail --silent --show-error http://localhost:9632/product/api/v1/products >/dev/null
echo "Smoke test passed."
