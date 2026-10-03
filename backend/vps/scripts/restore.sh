#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ $# -ne 1 ]]; then
  echo "Usage: bash scripts/restore.sh /opt/nexora/backups/YYYYMMDDTHHMMSSZ" >&2
  exit 1
fi

BACKUP_DIR="$1"
if [[ ! -d "$BACKUP_DIR" ]]; then
  echo "[nexora-restore] Backup directory not found: $BACKUP_DIR" >&2
  exit 1
fi

for file in postgres.sql.gz uploads.tar.gz manifest.json SHA256SUMS; do
  if [[ ! -f "$BACKUP_DIR/$file" ]]; then
    echo "[nexora-restore] Missing backup file: $file" >&2
    exit 1
  fi
done

if [[ ! -f .env ]]; then
  echo "[nexora-restore] Missing backend/vps/.env" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

POSTGRES_DB="${POSTGRES_DB:-nexora}"
POSTGRES_USER="${POSTGRES_USER:-nexora}"

echo "[nexora-restore] Verifying backup checksums"
(
  cd "$BACKUP_DIR"
  sha256sum -c SHA256SUMS
)

echo "[nexora-restore] This will replace the current database and uploads."
read -r -p "Type RESTORE to continue: " CONFIRM
if [[ "$CONFIRM" != "RESTORE" ]]; then
  echo "[nexora-restore] Aborted"
  exit 1
fi

docker compose ps >/dev/null

echo "[nexora-restore] Restoring PostgreSQL database: $POSTGRES_DB"
zcat "$BACKUP_DIR/postgres.sql.gz" | docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"

echo "[nexora-restore] Restoring uploads volume"
docker compose exec -T api sh -c 'rm -rf /app/uploads/* && mkdir -p /app/uploads'
cat "$BACKUP_DIR/uploads.tar.gz" | docker compose exec -T api tar -C /app -xzf -

echo "[nexora-restore] Restarting API"
docker compose restart api

echo "[nexora-restore] Restore complete"
