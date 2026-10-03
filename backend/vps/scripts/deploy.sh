#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

docker compose pull postgres || true
docker compose build api
docker compose up -d

docker compose ps
curl -fsS http://127.0.0.1:8080/health || true
