#!/usr/bin/env bash
set -eu

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
mode="${1:-report}"

if ! command -v rg >/dev/null 2>&1; then
  echo "错误：需要安装 ripgrep (rg) 才能执行兼容性盘点。" >&2
  exit 2
fi

if [ "$mode" != "report" ] && [ "$mode" != "--check" ]; then
  echo "用法：$0 [report|--check]" >&2
  exit 2
fi

cd "$repo_root"

scan() {
  label="$1"
  pattern="$2"
  matches="$(rg -n --glob '!**/target/**' --glob '*.{java,xml,yml,yaml,properties}' "$pattern" . || true)"
  if [ -n "$matches" ]; then
    count="$(printf '%s\n' "$matches" | wc -l | tr -d ' ')"
  else
    count=0
  fi
  printf '%-34s %6s\n' "$label" "$count"
  total=$((total + count))
}

scan_files() {
  label="$1"
  glob="$2"
  matches="$(rg --files -g "$glob" -g '!**/target/**' . || true)"
  if [ -n "$matches" ]; then
    count="$(printf '%s\n' "$matches" | wc -l | tr -d ' ')"
  else
    count=0
  fi
  printf '%-34s %6s\n' "$label" "$count"
  total=$((total + count))
}

total=0
echo "框架升级兼容性盘点（匹配行数）"
scan "Jakarta 待迁移 API" 'javax\.(persistence|servlet|validation|mail|annotation)'
scan "Neo4j OGM/旧 Repository API" 'org\.neo4j\.ogm|GraphRepository|Neo4jOperations'
scan "Eureka/Config/Zuul/旧 Feign" 'spring-cloud-starter-(eureka|config|zuul|feign)|EnableEureka|EnableZuul|EnableFeign'
scan "Springfox" 'io\.springfox|springfox\.'
scan "旧 MySQL 驱动" 'mysql-connector-java|com\.mysql\.jdbc\.Driver'
scan "Spotify Docker 插件" 'com\.spotify|docker-maven-plugin'
scan_files "bootstrap 配置文件" '**/bootstrap.*'
printf '%-34s %6s\n' "合计" "$total"

if [ "$mode" = "--check" ] && [ "$total" -ne 0 ]; then
  echo "检查失败：仍存在框架升级阻断项。" >&2
  exit 1
fi
