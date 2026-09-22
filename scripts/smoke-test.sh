#!/bin/sh
set -eu

wait_for_url() {
  name=$1
  url=$2
  shift 2
  echo "Waiting for ${name}: ${url}"
  curl --fail --silent --show-error --retry 30 --retry-delay 2 --retry-connrefused "$@" "$url" >/dev/null
}

wait_for_url nacos http://localhost:8848/nacos/
wait_for_url gateway http://localhost:9632/actuator/health

curl --fail --silent --show-error http://localhost:9632/product/api/v1/products >/dev/null
echo "Smoke test passed."
