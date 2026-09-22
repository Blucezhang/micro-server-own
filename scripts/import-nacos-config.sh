#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
nacos_server="${NACOS_SERVER:-http://localhost:8848}"
nacos_namespace="${NACOS_NAMESPACE:-public}"
nacos_group="${NACOS_GROUP:-MICRO_SERVER}"
nacos_username="${NACOS_USERNAME:?set NACOS_USERNAME}"
nacos_password="${NACOS_PASSWORD:?set NACOS_PASSWORD}"

for command_name in curl rg; do
  command -v "$command_name" >/dev/null 2>&1 || {
    echo "错误：需要命令 $command_name。" >&2
    exit 2
  }
done

login_response="$(curl --fail --silent --show-error --request POST \
  "${nacos_server%/}/nacos/v3/auth/user/login" \
  --data-urlencode "username=$nacos_username" \
  --data-urlencode "password=$nacos_password")"
access_token="$(printf '%s' "$login_response" | rg -o '"accessToken"\s*:\s*"[^"]+"' | sed -E 's/.*"([^"]+)"/\1/' || true)"

if [ -z "$access_token" ]; then
  echo "错误：Nacos 登录未返回 accessToken。" >&2
  exit 1
fi

for config_file in "$repo_root"/deploy/nacos/config/*.yaml; do
  data_id="$(basename "$config_file")"
  app_name="${data_id%-local.yaml}"
  response="$(curl --fail --silent --show-error --request POST \
    "${nacos_server%/}/nacos/v3/admin/cs/config" \
    --header "accessToken: $access_token" \
    --data-urlencode "namespaceId=$nacos_namespace" \
    --data-urlencode "groupName=$nacos_group" \
    --data-urlencode "dataId=$data_id" \
    --data-urlencode "appName=$app_name" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$config_file")"
  if ! printf '%s' "$response" | rg -q '"data"\s*:\s*true'; then
    echo "错误：发布 $data_id 失败：$response" >&2
    exit 1
  fi
  echo "已发布 $data_id 到 $nacos_namespace/$nacos_group"
done
